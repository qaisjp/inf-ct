#include "llvm/Pass.h"
#include "llvm/IR/Function.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/IR/LegacyPassManager.h"
#include "llvm/Transforms/IPO/PassManagerBuilder.h"
#include "llvm/Transforms/Utils/Local.h"
#include <set>
#include <vector>

bool DEBUG_MODE = false;
bool livenessPrinted = false;

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

    bool safeToRemove(Instruction* I) {
      return !isa<CallInst>(I) &&
        !I->mayHaveSideEffects() &&
        !I->isTerminator();
    }

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

    void outputLiveness(Function &F, ValueSetMap in) {
      if (livenessPrinted) {
        return;
      }
      livenessPrinted = true;

      for (BasicBlock &bb : F) {
        for (auto iter = bb.begin(); iter != bb.end(); ++iter) {
          Instruction* I = &*iter;

          // Ignore phinodes
          if (isa<PHINode>(I)) {
            continue;
          }

          errs() << "{";
          auto i = 0;
          auto max = in[I].size();
          for (auto V : in[I]) {
            V->printAsOperand(errs(), false);
            i++;
            if (i < max) {
              errs() << ",";
            }
          }
          errs() << "}\n";

          if (DEBUG_MODE)
            errs() << "\t" << *I << "\n";
        }
      }

      errs() << "{}\n\n";
    }

    // searchAndDestroy returns true if something changed
    bool searchAndDestroy(Function &F) {
      SmallVector<Instruction*, 16> ul;

      ValueSetMap in, prevIn, out, prevOut, diff;

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

            ValueSet users;
            if (isa<PHINode>(I)) {
              auto succPhi = dyn_cast<PHINode>(I);
              for (auto i=0; i<succPhi->getNumIncomingValues(); i++) {
                auto incomingVal = succPhi->getIncomingValue(i);
                if (isa<Instruction>(incomingVal) || isa<Argument>(incomingVal)) {
                  auto vb = succPhi->getIncomingBlock(i);
                  phiMap[succPhi][vb].insert(incomingVal);
                }
              }
            } else {
              users = getInstructionUsers(I);
            }

            // Copy out into outCopied
            // Then remove current instruction from outCopied (out[n] - def[n])
            std::copy(out[I].begin(), out[I].end(), std::inserter(diff[I], diff[I].begin()));
            diff[I].erase(I);

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
                        diff[I].begin(), diff[I].end(),
                        std::inserter(inDest, inDest.begin()));

            in[I] = inDest; // in[I] = use[n] U (out[n] - def[n])

            // If successors>0, then terminator.
            // BUT TERMINATORS DON'T ALWAYS HAVE SUCCESSORS!
            // errs() << "Terminator: " << toboolstr(I->isTerminator()) << "\tSuccs: " << I->getNumSuccessors() << "\n";
            assert((I->getNumSuccessors()>0) ? I->isTerminator() : true);

            // Part 2 of Solve Data-Flow Equations
            InstructionSet successors;
            std::set<PHINode*> phiSuc;
            if (I->isTerminator()) {
              for(size_t i = 0; i < I->getNumSuccessors(); i++)
              {
                BasicBlock* succBB = I->getSuccessor(i);
                auto it = succBB->begin();
                auto succI = &*it;
                successors.insert(succI);

                // errs() << "Current: " << *succI << "; phi:" << isa<PHINode>(succI) << ";\n";

                if (isa<PHINode>(succI)) {
                  for (it++; it != succBB->end(); it++) {
                    if (!isa<PHINode>(&*it)) {
                      break;
                    }
                    if (DEBUG_MODE)
                      errs() << "- " << *it << ";\n";
                    phiSuc.insert(dyn_cast<PHINode>(&*it));
                  }
                }
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
              ValueSet swapped;

              if (isPHINode(successor)) {
                auto sucPhi = dyn_cast<PHINode>(successor);
                if (I->isTerminator()) { // if came from branch (not another phinode)
                  auto s = phiMap[sucPhi][&*bb];

                  std::set_union(s.begin(), s.end(),
                            diff[sucPhi].begin(), diff[sucPhi].end(),
                            std::inserter(swapped, swapped.begin()));
                } else {
                  swapped = diff[sucPhi];
                }
              } else {
                swapped = in[successor];
              }

              std::set_union(swapped.begin(), swapped.end(),
                          outDest.begin(), outDest.end(),
                          std::inserter(newOutDest, newOutDest.begin()));

              outDest = newOutDest; // outDest = [1,2] U in[s]
            }

            if (!phiSuc.empty()) {
              ValueSet phiUnion;
              for (auto V : phiSuc) {
                auto s = phiMap[V][&*bb];
                ValueSet newPhiUnion;
                std::set_union(s.begin(), s.end(),
                            phiUnion.begin(), phiUnion.end(),
                            std::inserter(newPhiUnion, newPhiUnion.begin()));
                phiUnion = newPhiUnion;
              }

              ValueSet newNewOutDest;
              std::set_union(phiUnion.begin(), phiUnion.end(),
                          outDest.begin(), outDest.end(),
                          std::inserter(newNewOutDest, newNewOutDest.begin()));
              outDest = newNewOutDest;
            }

            out[I] = outDest;
          }
        }
      } while (!(prevIn == in && prevOut == out));

      // Output that shit
      if (DEBUG_MODE)
        errs() << "\nOutput everything:\n";
      outputLiveness(F, in);

      // Find dead instructions
      if (DEBUG_MODE)
        errs() << "\n\nLooping through instructions:\n";

      for (Function::iterator bb = F.begin(); bb != F.end(); ++bb) {
        for (BasicBlock::iterator iter = bb->begin(); iter != bb->end(); ++iter) {
          Instruction* I = &*iter;
          bool isDead = (out[I].find(I) == out[I].end())
            && safeToRemove(I);
          if (isDead) {
            ul.push_back(I);
          }
        }
      }

      if (DEBUG_MODE)
        errs() << "\nNow erasing:\n";

      // Erase each instruction
      // for (auto it = ul.rbegin(); it != ul.rend(); it++) {
      for (Instruction* I : ul) {
      //   Instruction* I = *it;
        if (DEBUG_MODE) {
          errs() << "- erasing: " << *I << "\n";
        }
        I->eraseFromParent();
      }

      if (DEBUG_MODE)
        errs() << "- done!\n";

      return !ul.empty();
    }

    virtual bool runOnFunction(Function &F) {
      // Set debug mode
      if (const char* env_p = std::getenv("QDEBUG")) {
        DEBUG_MODE=true;
      }

      int pass = 0;
      auto fName = F.getName();

      livenessPrinted = false;
      bool changed = false;
      bool everChanged = false;
      do {
        pass += 1;
        if (DEBUG_MODE)
          errs() << "\n# Function: " << fName << " (pass " << pass << ")\n";
        changed = searchAndDestroy(F);
        everChanged |= changed;
      } while (changed);

      if (DEBUG_MODE)
        errs() << "\n# Done with " << fName << "\n\n######\n";

      return everChanged;
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
