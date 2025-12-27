; =====================================================
; TEST DE SBC (Soustraction avec Carry/Borrow)
; Objectif : vérifier que le Carry (Borrow) est bien pris
;            en compte lors d'une soustraction.
; =====================================================

; === 1. PRÉPARATION DE LA MÉMOIRE ===
LDX #$1000        ; Charger l’adresse $1000 dans le registre X
LDA #$10          ; Charger la valeur $10 dans l’accumulateur A
STA ,X            ; Stocker A à l’adresse pointée par X
                  ; → RAM[$1000] = $10

; === 2. CRÉATION DU BORROW (CARRY = 1) ===
LDA #$00          ; Charger 0 dans A
SUBA #$01         ; 0 - 1 = $FF
                  ; → Le résultat est négatif
                  ; → Le bit Carry est positionné à 1
                  ; → Carry = Borrow pour les soustractions ✅

; === 3. TEST DE SBC (Soustraction avec Carry) ===
; ⚠️ Important : LDB ne modifie PAS le Carry
LDB #$50          ; Charger $50 dans l’accumulateur B
SBCB ,X           ; B = B - [X] - Carry
                  ; B = $50 - $10 - 1
                  ; B = $40 - 1
                  ; B = $3F

; === 4. SAUVEGARDE DU RÉSULTAT ===
STB $2000         ; Stocker le résultat dans la mémoire
                  ; → RAM[$2000] = $3F

END               ; Fin du programme