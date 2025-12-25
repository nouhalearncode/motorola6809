; 1. Creer Pointer
LDX #$CAFE
STX $1000   ; RAM[1000] = Pointer vers CAFE

; 2. Stocker Valeur
LDA #$AA
STA $CAFE   ; RAM[CAFE] = AA

; 3. Indirect
LDX #$1000  ; X pointe vers le Pointer
LDB [,X]    ; B = [[1000]] = [CAFE] = AA
END