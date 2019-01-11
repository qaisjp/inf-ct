#include "llvm/Pass.h"
#include "llvm/IR/Function.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/IR/LegacyPassManager.h"
#include "llvm/Transforms/IPO/PassManagerBuilder.h"
#include "llvm/Transforms/Utils/Local.h"
#include <vector>

using namespace llvm;

bool stayinAlive(Instruction* I) {
  if (I->mayHaveSideEffects()) {
    return true;
  }

  // I->isTerminator();

  // auto block = I->getSuccessor();
  // return isInstructionTriviallyDead(I);
  return false;
}

namespace {
  typedef std::set<Instruction*> InstructionSet;
  typedef std::map<Instruction*, InstructionSet> InstructionSetMap;

  bool str_neq(const char* a, const char* b) {
    return strcmp(a, b) != 0;
  }

  InstructionSet getInstructionUsers(Instruction* I) {
    InstructionSet users;
    for (auto i = I->op_begin(); i != I->op_end(); i++) {
      if (isa<Instruction>(&*i)) {
        auto in = dyn_cast<Instruction>(&*i);
        // errs() << "-address in: " << in << "\n";
        // errs() << "-- " << *in << "\n";
        users.insert(in);
      }
    }
    return users;
  }

  struct SkeletonPass : public FunctionPass {
    static char ID;
    SkeletonPass() : FunctionPass(ID) {}

    // searchAndDestroy returns true if something changed
    bool searchAndDestroy(Function &F) {
      SmallVector<Instruction*, 16> ul;

      InstructionSetMap in, inPrime, out, outPrime;

      for (BasicBlock &bb : F) {
        for (BasicBlock::iterator i = bb.begin(); i != bb.end(); ++i) {
          Instruction* I = &*i;
          in[I] = InstructionSet();
          out[I] = InstructionSet();
        }
      }

      // Calculate in and out sets
      int count = 0;
      do {
        count+=1;
        errs() << "Count: " << count << "\n";

        for (BasicBlock &bb : F) {
          for (BasicBlock::iterator iter = bb.begin(); iter != bb.end(); ++iter) {
            Instruction* I = &*iter;
            inPrime[I] = in[I];
            outPrime[I] = out[I];

            InstructionSet users = getInstructionUsers(I);

            // Copy out into outCopied
            // Then remove current instruction from outCopied (out[n] - def[n])
            InstructionSet outCopied;
            std::copy(out[I].begin(), out[I].end(), std::inserter(outCopied, outCopied.begin()));
            outCopied.erase(I);

            // use[n] U (out[n] - def[n])
            InstructionSet inDest;
            std::set_union(users.begin(), users.end(),
                       outCopied.begin(), outCopied.end(),
                       std::inserter(inDest, inDest.begin()));

            in[I] = inDest; // in[I] = use[n] U (out[n] - def[n])

            // Part 2 of Solve Data-Flow Equations
            InstructionSet successors;
            if (I->isTerminator()) {
              for(size_t i = 0; i < I->getNumSuccessors(); i++)
              {
                BasicBlock* bb = I->getSuccessor(i);
                successors.insert(&*bb->begin());
              }
            } else {
              // Peek at the next item
              auto peek = iter;
              ++peek;

              Instruction* successor = &*peek;
              bool reachedEnd = successor == &*bb.end();

              // errs() << "Reached end: " << reachedEnd << "\n";

              if (!reachedEnd) {
                successors.insert(successor);
              }
            }

            InstructionSet outDest; // [1, 2]
            for (Instruction* successor : successors) {
              InstructionSet newDest; // [1, 2] U in[s]
              std::set_union(in[successor].begin(), in[successor].end(),
                        outDest.begin(), outDest.end(),
                        std::inserter(newDest, newDest.begin()));

              outDest = newDest; // outDest = [1,2] U in[s]
            }

            out[I] = outDest;
          }
        }
      } while (!(inPrime == in && outPrime == out));

      InstructionSet outs = out.end()->second;

      // Find dead instructions
      errs() << "\n\nNow eliminating:\n";
      for (BasicBlock &bb : F) {
        for (BasicBlock::iterator iter = bb.begin(); iter != bb.end(); ++iter) {
          Instruction* I = &*iter;
          const bool outs_contains = outs.find(I) != outs.end();

          if (!outs_contains) {
          // if (ourIsDead(&I)) {
            errs() << "- dead: ";
            I->printAsOperand(errs());
            errs() << "\n";
            ul.push_back(I);
          } else {
            errs() << "- alive: ";
            I->printAsOperand(errs());
            errs() << "\n";
          }
        }
      }

      // Erase each instruction
      for (Instruction* I : ul) {
        I->eraseFromParent();
      }

      return !ul.empty();
    }

    virtual bool runOnFunction(Function &F) {
      int pass = 0;
      auto fName = F.getName();

      // do {
        // pass += 1;
        errs() << "\n# Function: " << fName << " (pass " << pass << ")\n";
        searchAndDestroy(F);
      // } while (searchAndDestroy(F));

      errs() << "\n# Done with " << fName << "\n\n######\n";

      return true;
    }
  };
}

char SkeletonPass::ID = 0;

// Register the pass
__attribute__((unused)) static RegisterPass<SkeletonPass> X(
  "live", "Dead code elimination with liveness analysis"
);

// Automatically enable the pass.
// http://adriansampson.net/blog/clangpass.html
static void registerSkeletonPass(const PassManagerBuilder &,
                         legacy::PassManagerBase &PM) {
  PM.add(new SkeletonPass());
}
static RegisterStandardPasses
  RegisterMyPass(PassManagerBuilder::EP_EarlyAsPossible,
                 registerSkeletonPass);
