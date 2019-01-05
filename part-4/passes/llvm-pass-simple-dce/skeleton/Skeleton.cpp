#include "llvm/Pass.h"
#include "llvm/IR/Function.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/IR/LegacyPassManager.h"
#include "llvm/Transforms/IPO/PassManagerBuilder.h"
#include <vector>

using namespace llvm;

int counter = 0;

/*
SmallVector<Instruction*, 16> ul;

while changed {
  changed = false
  for (instruction i) {
    if i.isTriviallyDead() {
      changed = true
      ul.push(i)
    }
  }

  while (!ul.empty()) {
    node = ul.pop();
    node.replaceAllwith(node.getCurrentValue(0)) // i think?
    nodeblah
}
*/

namespace {
  struct SkeletonPass : public FunctionPass {
    static char ID;
    SkeletonPass() : FunctionPass(ID) {}

    virtual bool runOnFunction(Function &F) {
      int instructions = F.getInstructionCount();
      int ourInstructions = 0;

      for (Function::iterator bb = F.begin(); bb != F.end(); ++bb) {
        for (BasicBlock::iterator i = bb->begin(); i != bb->end(); ++i) {
          Instruction* inst = &*i;
          ourInstructions++;
        }
      }

      counter++;
      errs() << "Function " << F.getName() << " (" << counter << ") \tInstructions:\t" << instructions << "\tOurs: " << ourInstructions << "\n";
      return false;
    }
  };
}

char SkeletonPass::ID = 0;

// Automatically enable the pass.
// http://adriansampson.net/blog/clangpass.html
static void registerSkeletonPass(const PassManagerBuilder &,
                         legacy::PassManagerBase &PM) {
  PM.add(new SkeletonPass());
}
static RegisterStandardPasses
  RegisterMyPass(PassManagerBuilder::EP_EarlyAsPossible,
                 registerSkeletonPass);
