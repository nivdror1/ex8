// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/07/StackArithmetic/StackTest/StackTest.tst

load CopyAtoR13.asm,
output-file CopyAtoR13.out,
//compare-to StackTest.cmp,

set A 30,
set RAM[0] 256,  // initializes the stack pointer

repeat 1000 {    // enough cycles to complete the execution
  ticktock;
}

// outputs the stack pointer (RAM[0]) and 
// the stack contents: RAM[256]-RAM[265]
output;
output-list RAM[261]%D2.6.2 RAM[262]%D2.6.2 RAM[263]%D2.6.2 RAM[264]%D2.6.2 RAM[265]%D2.6.2;
output;
