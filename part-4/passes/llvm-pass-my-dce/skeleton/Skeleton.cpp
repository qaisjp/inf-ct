#include "llvm/Pass.h"
#include "llvm/IR/Function.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/IR/LegacyPassManager.h"
#include "llvm/Transforms/IPO/PassManagerBuilder.h"
#include "llvm/Transforms/Utils/Local.h"
#include <vector>

using namespace llvm;

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

bool ourIsDead(Instruction* I) {
  return isInstructionTriviallyDead(I);
}

namespace {
  struct SkeletonPass : public FunctionPass {
    static char ID;
    SkeletonPass() : FunctionPass(ID) {}

    // searchAndDestroy returns true if something changed
    bool searchAndDestroy(Function &F) {
      SmallVector<Instruction*, 16> ul;

      // Find dead instructions
      for (Function::iterator bb = F.begin(); bb != F.end(); ++bb) {
        for (BasicBlock::iterator i = bb->begin(); i != bb->end(); ++i) {
          Instruction* inst = &*i;

          if (ourIsDead(inst)) {
            errs() << "instruction dead: ";
            inst->printAsOperand(errs());
            errs() << "\n";
            ul.push_back(inst);
          } else {
            errs() << "instruction alive: ";
            inst->printAsOperand(errs());
            errs() << "\n";
          }
        }
      }

      // Erase each instruction
      for (Instruction* inst : ul) {
        errs() << "instruction removed\n";
        inst->eraseFromParent();
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
