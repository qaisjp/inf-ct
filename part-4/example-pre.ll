define i32 @sum(i32 %a, i32 %b) nounwind readnone ssp {
entry:
; {%a, %b} is in set of below line
 %0 = icmp slt i32 %a, %b
; {%a, %b, %0}
 br i1 %0, label %bb.nph, label %bb2
bb.nph: ; preds = %entry
; {%a, %b}
 %tmp = sub i32 %b, %a
; {%a, %tmp}
 br label %bb
bb: ; preds = %bb, %bb.nph
 %indvar = phi i32 [ 0, %bb.nph ], [ %indvar.next, %bb ]
 %res.05 = phi i32 [ 1, %bb.nph ], [ %1, %bb ]
; {%a, %tmp, %indvar, %res.05}
 %i.04 = add i32 %indvar, %a
; {%a, %tmp, %indvar, %res.05, %i.04}
 %1 = mul nsw i32 %res.05, %i.04
; {%a, %tmp, %indvar, %1}
 %indvar.next = add i32 %indvar, 1
; {%a, %tmp, %1, %indvar.next}
 %exitcond = icmp eq i32 %indvar.next, %tmp
; {%a, %tmp, %1, %indvar.next, %exitcond}
 br i1 %exitcond, label %bb2, label %bb
bb2: ; preds = %bb, %entry
 %res.0.lcssa = phi i32 [ 1, %entry ], [ %1, %bb ]
; {%res.0.lcssa}
 ret i32 %res.0.lcssa
; {}
}
