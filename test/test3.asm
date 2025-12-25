; === 1. SETUP REGISTERS ===
LDD #$DEAD
LDX #$3000

; === 2. SETUP POINTERS (Circular Ref) ===
STD ,X      ; RAM[3000] = DE AD
STX $DEAD   ; RAM[DEAD] = 30 00
            ; Now : [3000]->DEAD and [DEAD]->3000

; === 3. INDIRECT LOAD 16-BIT (L'Crash Test) ===
; LDY = [[X]] = [[3000]] = [DEAD] = 3000
LDY [,X]    ; Y must become 3000

; === 4. MATH 16-BITS ===
ADDD #$1111 ; D = DEAD + 1111 = EFBE
SUBD #$1000 ; D = EFBE - 1000 = DFBE

; === 5. LEA & PCR (Adressage) ===
; LEAU $10,PCR : U = CurrentPC + Offset.
LEAU $0010,PCR

; === 6. FINAL SHUFFLE ===
TFR Y,X     ; X = 3000
EXG A,B     ; D was DFBE (A=DF, B=BE).
            ; Swap -> A=BE, B=DF.
            ; D Final = BEDF.
END