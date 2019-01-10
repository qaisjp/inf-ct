#include "llvm/Pass.h"
#include "llvm/IR/Function.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/IR/LegacyPassManager.h"
#include "llvm/Transforms/IPO/PassManagerBuilder.h"
#include "llvm/Transforms/Utils/Local.h"
#include <vector>

using namespace llvm;

bool ourIsDead(Instruction* I) {
  if (I->mayHaveSideEffects()) {
    return false;
  }

  // I->isTerminator();

  // auto block = I->getSuccessor();
  return isInstructionTriviallyDead(I);
}

namespace {
  typedef std::set<Instruction*> InstructionSet;
  typedef std::map<Instruction*, InstructionSet> InstructionSetMap;

  InstructionSet getInstructionUsers(Instruction &I) {
    InstructionSet users;
    for (User* U : I.users()) {
      if (Instruction* I = dyn_cast<Instruction>(U)) {
        users.insert(I);
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

      for (BasicBlock &BB : F) {
        for (Instruction &inst : BB) {
          in[&inst] = InstructionSet();
          out[&inst] = InstructionSet();
        }
      }

      do {
        for (BasicBlock &BB : F) {
          for (Instruction &inst : BB) {
            Instruction* I = &inst;
            inPrime[I] = in[I];
            outPrime[I] = out[I];

            InstructionSet users = getInstructionUsers(inst);

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

            in[I] = inDest;

            // Part 2 of Solve Data-Flow Equations
          }
        }

        // errs() << "equality: ";
        // errs() << (in == out);
        // errs() << "!!!!!!\n";

      } while (!(inPrime == in && outPrime == out));

      // Find dead instructions
      errs() << "\n\nNow eliminating:\n";
      for (BasicBlock &BB : F) {
        for (Instruction &I : BB) {
          if (ourIsDead(&I)) {
            errs() << "- dead: ";
            I.printAsOperand(errs());
            errs() << "\n";
            ul.push_back(&I);
          } else {
            errs() << "- alive: ";
            I.printAsOperand(errs());
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
      do {
        pass += 1;
        errs() << "\n# Function: " << F.getName() << " (pass " << pass << ")\n";
      } while (searchAndDestroy(F));

      errs() << "\n--------\n";

      return false;
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
