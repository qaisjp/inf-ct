#include "llvm/Pass.h"
#include "llvm/IR/Function.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/IR/LegacyPassManager.h"
#include "llvm/Transforms/IPO/PassManagerBuilder.h"
#include "llvm/Transforms/Utils/Local.h"
#include <set>
#include <vector>

bool DEBUG_MODE = true;

#define toboolstr(i) ((i == 0) ? "false" : "true")

using namespace llvm;

namespace {
  typedef std::set<Value*> ValueSet;
  typedef std::set<Instruction*> InstructionSet;
  typedef std::map<Value*, ValueSet> ValueSetMap;

  bool str_eq(const char* a, const char* b) {
    return strcmp(a, b) == 0;
  }

  bool isPHINode(Value* v) {
    return isa<PHINode>(v);
  }

  ValueSet getInstructionUsers(Instruction* I) {
    ValueSet users;
    for (auto i = I->op_begin(); i != I->op_end(); i++) {
      if (isa<Instruction>(&*i) || isa<Argument>(&*i)) {
        auto in = dyn_cast<Value>(&*i);
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

    #define printSet(setContent, asP) _printSet(#setContent, setContent, asP)
    void _printSet(const char* thing, ValueSet s, bool asPointer) {
      errs() << thing << ": ";
      for (auto it = s.begin(); it != s.end(); it++) {
        if (asPointer) {
          errs() << *it << ", ";
        } else {
          errs() << **it << ", ";
        }
      }
      errs() << "\n";
    }

    // searchAndDestroy returns true if something changed
    bool searchAndDestroy(Function &F) {
      SmallVector<Instruction*, 16> ul;

      ValueSetMap in, prevIn, out, prevOut;

      for (BasicBlock &bb : F) {
        for (BasicBlock::iterator i = bb.begin(); i != bb.end(); ++i) {
          Instruction* I = &*i;
          in[I] = ValueSet();
          out[I] = ValueSet();
        }
      }

      std::map<PHINode*, std::map<BasicBlock*,ValueSet>> phiMap;

      // Calculate in and out sets
      int count = 0;
      do {
        count+=1;
        if (DEBUG_MODE)
          errs() << "Count: " << count << "\n";

        for (Function::iterator bb = F.begin(); bb != F.end(); ++bb) {
          for (BasicBlock::iterator iter = bb->begin(); iter != bb->end(); ++iter) {
            Instruction* I = &*iter;
            prevIn[I] = in[I];
            prevOut[I] = out[I];

            // if (isa<PHINode>(I)) {
            //   auto p = dyn_cast<PHINode>(I);
            //   in[bb->getFirstNonPHI()].insert(I);
            //   continue;
            // }

            ValueSet users = getInstructionUsers(I);

            // Copy out into outCopied
            // Then remove current instruction from outCopied (out[n] - def[n])
            ValueSet outCopied;
            std::copy(out[I].begin(), out[I].end(), std::inserter(outCopied, outCopied.begin()));
            outCopied.erase(I);

            // Debug outCopied is correct
            // errs() << "#out[I] = " << out[I].size() << "\t#outCopied = " << outCopied.size() <<"\n";
            // if (out[I].size() > 0) {
            //   errs() << "this: " << I << "\n";
            //   printSet(out[I], true);
            //   printSet(outCopied, true);
            // }
            // errs() << "\n";

            // use[n] U (out[n] - def[n])
            ValueSet inDest;
            std::set_union(users.begin(), users.end(),
                        outCopied.begin(), outCopied.end(),
                        std::inserter(inDest, inDest.begin()));

            in[I] = inDest; // in[I] = use[n] U (out[n] - def[n])

            // If successors>0, then terminator.
            // BUT TERMINATORS DON'T ALWAYS HAVE SUCCESSORS!
            // errs() << "Terminator: " << toboolstr(I->isTerminator()) << "\tSuccs: " << I->getNumSuccessors() << "\n";
            assert((I->getNumSuccessors()>0) ? I->isTerminator() : true);

            // Part 2 of Solve Data-Flow Equations
            InstructionSet successors;
            if (I->isTerminator()) {
              for(size_t i = 0; i < I->getNumSuccessors(); i++)
              {
                BasicBlock* succBB = I->getSuccessor(i);
                auto succI = &*succBB->begin();
                auto succPhi = dyn_cast<PHINode>(succI);

                phiMap[succPhi][succBB] = out[I];
              }
            } else {
              // Peek at the next item
              auto peek = iter;
              ++peek;

              Instruction* successor = &*peek;

              // We can never have reached the end because is terminator checks this for us
              bool reachedEnd = successor == &*bb->end();
              assert(!reachedEnd);

              successors.insert(successor);
            }

            ValueSet outDest; // [1, 2]
            for (Instruction* successor : successors) {
              ValueSet newOutDest; // [1, 2] U in[s]

              if (isPHINode(successor)) {
                auto sucPhi = dyn_cast<PHINode>(successor);
                auto s = phiMap[sucPhi][&*bb];
                std::set_union(s.begin(), s.end(),
                          outDest.begin(), outDest.end(),
                          std::inserter(newOutDest, newOutDest.begin()));
              } else {
                std::set_union(in[successor].begin(), in[successor].end(),
                          outDest.begin(), outDest.end(),
                          std::inserter(newOutDest, newOutDest.begin()));
              }

              outDest = newOutDest; // outDest = [1,2] U in[s]
            }

            out[I] = outDest;
          }
        }
      } while (!(prevIn == in && prevOut == out));

      // Output that shit
      if (DEBUG_MODE)
        errs() << "\nOutput everything:\n";
      for (BasicBlock &bb : F) {
        for (auto iter = bb.begin(); iter != bb.end(); ++iter) {
          Instruction* I = &*iter;

          // Ignore phinodes
          if (isa<PHINode>(I)) {
            continue;
          }

          errs() << "{";
          int i=0;
          for (auto V : in[I]) {
            V->printAsOperand(errs(), false);

            i++;
            if (i < in[I].size()) {
              errs() << ",";
            }
          }
          errs() << "}\n";

          if (DEBUG_MODE)
            errs() << "\t" << *I << "\n";
        }
      }

      errs() << "{}";

      // Find dead instructions
      if (DEBUG_MODE)
        errs() << "\n\nLooping through instructions:\n";
      ValueSet currentLive, currentDead;

      for (BasicBlock &bb : F) {
        for (auto iter = bb.rbegin(); iter != bb.rend(); ++iter) {
          Instruction* I = &*iter;

          ValueSet outs = out[I];
          ValueSet ins = in[I];
          currentLive.clear();

          std::set_difference(outs.begin(), outs.end(),
                      currentDead.begin(), currentDead.end(),
                      std::inserter(currentLive, currentLive.begin()));

          bool isDead = (currentLive.find(I) == currentLive.end())
            && I->isSafeToRemove();
          auto opName = (I->getOpcodeName());
          currentDead.clear();
          if (
            !outs.empty() && isDead
            && !str_eq(opName, "ret") && !str_eq(opName, "br")) {

            if (DEBUG_MODE)
              errs() << "- dead: ";
            I->printAsOperand(errs());
            if (DEBUG_MODE)
              errs() << "\n";
            ul.push_back(I);

            std::set_difference(ins.begin(), ins.end(),
                      currentLive.begin(), currentLive.end(),
                      std::inserter(currentDead, currentDead.begin()));

          } else {
            if (DEBUG_MODE)
              errs() << "- alive: ";
            I->printAsOperand(errs());
            if (DEBUG_MODE)
              errs() << "\n";
          }
        }
      }

      if (DEBUG_MODE)
        errs() << "\nNow erasing:\n";

      // Erase each instruction
      for (Instruction* I : ul) {
        if (DEBUG_MODE)
          errs() << "- erasing: ";
        I->printAsOperand(errs(), false);
        if (DEBUG_MODE)
          errs() << "\n";
        I->eraseFromParent();
      }

      if (DEBUG_MODE)
        errs() << "- done!\n";

      return !ul.empty();
    }

    virtual bool runOnFunction(Function &F) {
      int pass = 0;
      auto fName = F.getName();

      // do {
        // pass += 1;
        if (DEBUG_MODE)
          errs() << "\n# Function: " << fName << " (pass " << pass << ")\n";
        searchAndDestroy(F);
      // } while (searchAndDestroy(F));

      if (DEBUG_MODE)
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
