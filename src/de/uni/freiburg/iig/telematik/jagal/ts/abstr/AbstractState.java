package de.uni.freiburg.iig.telematik.jagal.ts.abstr;

import java.io.Serializable;

import de.uni.freiburg.iig.telematik.jagal.graph.Vertex;

public abstract class AbstractState<O extends Object> extends Vertex<O> implements Serializable {

	private static final long serialVersionUID = 5995539025502231285L;

	public static final String DEFAULT_STATE_NAME = "<->";

	private boolean isLambdaState = false;

	protected AbstractState() {
		super(DEFAULT_STATE_NAME);
	}

	protected AbstractState(String name) {
		super(name);
	}

	protected AbstractState(String name, boolean isLambdaState) {
		this(name);
		setLambdaState(isLambdaState);
	}

	protected AbstractState(String name, O element) {
		super(name, element);
	}

	public boolean isLambdaState() {
		return isLambdaState;
	}

	public final void setLambdaState(boolean isLambdaState) {
		this.isLambdaState = isLambdaState;
	}

}
