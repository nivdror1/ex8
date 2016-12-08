//	pop to R13
// first stage - pop to D
@SP
M=M-1
A=M
D=M
//	second stage - write D to R13
@R13
M=D