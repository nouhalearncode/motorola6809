LDA #$FF    ; A = 255
ADDA #$01   ; A = 00 (Overflow). Carry=1. Zero=1.
STA $2000   ; RAM[2000] = 00
LDB #$10
ADCB #$20   ; B = 10 + 20 + 1(Carry) = 31
STB $2001   ; RAM[2001] = 31
END