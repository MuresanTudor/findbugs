/*
 * FindBugs - Find bugs in Java programs
 * Copyright (C) 2003, University of Maryland
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.umd.cs.findbugs;
import org.apache.bcel.classfile.Method;
import edu.umd.cs.pugh.visitclass.Constants2;

public class FindSpinLoop extends BytecodeScanningDetector implements   Constants2 {
    private static final boolean DEBUG = Boolean.getBoolean("findspinloop.debug");

    int stage = 0;
    int start;
    String name;
    private BugReporter bugReporter;

    public FindSpinLoop(BugReporter bugReporter) {
	this.bugReporter = bugReporter;
	}

    public void visit(Method obj) {
	if (DEBUG) System.out.println("Saw " + betterMethodName);
	stage = 0;
	}

    public void sawOpcode(int seen) {
	switch (seen) {
		case ALOAD_0: 
			if (DEBUG) System.out.println("   ALOAD_0 at PC " + PC);
			start = PC;
			stage  = 1;
			break;
		case GETFIELD:
			if (DEBUG) System.out.println("   getfield in stage " + stage);
			if (stage == 1) {
				stage = 2;
				name = betterClassConstant + "." + nameConstant;
				}
			else stage = 0;
			break;
		case IFNE:
		case IFEQ:
		case IFNULL:
		case IFNONNULL:
			if (DEBUG) System.out.println("   conditional branch in stage " + stage + " to " + branchTarget );
			if (stage == 2 && branchTarget == start)
				bugReporter.reportBug(new BugInstance("SP_SPIN_ON_FIELD", NORMAL_PRIORITY)
					.addClassAndMethod(this)
					.addReferencedField(this));
			stage = 0;
			break;
		default:
			stage = 0;
			break;
		}

		}
}
