//package org.anarres.cpp;
//
//import java.util.Stack;
//
//final class ConditionStateStack {
//	private final Stack<ConditionState> stateStack;
//
//	ConditionStateStack() {
//		this.stateStack = new Stack<>();
//		stateStack.push(new ConditionState(true, true, false));
//	}
//
//	boolean _if(boolean isActive) {
//		ConditionState parentState = stateStack.peek();
//		stateStack.push(new ConditionState(parentState.isActive(), isActive, true));
//		return true;
//	}
//
//
//	boolean _elif(boolean isActive) {
//		ConditionState currentState = stateStack.pop();
//		if (currentState.isIfRegion()) {
//			stateStack.push(new ConditionState(currentState.isParentActive() && !currentState.isActive(), isActive, true));
//			return true;
//		}
//		return false;
//	}
//
//	boolean _else() {
//		ConditionState currentState = stateStack.pop();
//		if (currentState.isIfRegion()) {
//			stateStack.push(new ConditionState(currentState.isParentActive(), !currentState.isActive(), false));
//			return true;
//		}
//		return false;
//	}
//
//	boolean _endif() {
//		stateStack.pop();
//		return !stateStack.empty();
//	}
//
//	public boolean isParentActive() {
//		return stateStack.peek().isParentActive();
//	}
//
//	public boolean isCurrentlyActive() {
//		return stateStack.peek().isActive();
//	}
//
//	public boolean isIfRegion() {
//		return stateStack.peek().isIfRegion();
//	}
//
//	private static class ConditionState {
//		private final int state;
//
//		ConditionState(boolean isParentActive, boolean isActive, boolean isIfRegion) {
//			this.state = (isParentActive ? 1 : 0) + (isActive ? 2 : 0) + (isIfRegion ? 4 : 0);
//		}
//
//		boolean isParentActive() {
//			return (state & 1) != 0;
//		}
//
//		boolean isActive() {
//			return (state & 2) != 0;
//		}
//
//		boolean isIfRegion() {
//			return (state & 4) != 0;
//		}
//	}
//}
