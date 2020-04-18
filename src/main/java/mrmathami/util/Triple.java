package mrmathami.util;

import java.io.Serializable;
import java.util.Objects;

public interface Triple<A, B, C> {
	static <A, B, C> Triple<A, B, C> mutableOf(A a, B b, C c) {
		return new MutableTriple<>(a, b, c);
	}

	static <A, B, C> Triple<A, B, C> immutableOf(A a, B b, C c) {
		return new ImmutableTriple<>(a, b, c);
	}

	A getA();

	A setA(A a) throws UnsupportedOperationException;

	B getB();

	B setB(B b) throws UnsupportedOperationException;

	C getC();

	C setC(C c) throws UnsupportedOperationException;
}

final class MutableTriple<A, B, C> implements Triple<A, B, C>, Serializable {
	private static final long serialVersionUID = -4622230648901699471L;
	private A a;
	private B b;
	private C c;

	MutableTriple(A a, B b, C c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}

	@Override
	public final A getA() {
		return a;
	}

	@Override
	public final A setA(A a) {
		final A oldA = this.a;
		this.a = a;
		return oldA;
	}

	@Override
	public final B getB() {
		return b;
	}

	@Override
	public final B setB(B b) {
		final B oldB = this.b;
		this.b = b;
		return oldB;
	}

	@Override
	public final C getC() {
		return c;
	}

	@Override
	public final C setC(C c) {
		final C oldC = this.c;
		this.c = c;
		return oldC;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (!(object instanceof Triple)) return false;
		final Triple<?, ?, ?> triple = (Triple<?, ?, ?>) object;
		return Objects.equals(a, triple.getA()) && Objects.equals(b, triple.getB()) && Objects.equals(c, triple.getC());
	}

	@Override
	public int hashCode() {
		return Objects.hash(a, b, c);
	}

	@Override
	public final String toString() {
		return "{ " + a + ", " + b + ", " + c + " }";
	}
}

final class ImmutableTriple<A, B, C> implements Triple<A, B, C>, Serializable {
	private static final long serialVersionUID = 1279580388589425867L;
	private final A a;
	private final B b;
	private final C c;

	ImmutableTriple(A a, B b, C c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}

	@Override
	public final A getA() {
		return a;
	}

	@Override
	public final A setA(A a) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Immutable triple can't be modified.");
	}

	@Override
	public final B getB() {
		return b;
	}

	@Override
	public final B setB(B b) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Immutable triple can't be modified.");
	}

	@Override
	public final C getC() {
		return c;
	}

	@Override
	public final C setC(C c) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Immutable triple can't be modified.");
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (!(object instanceof Triple)) return false;
		final Triple<?, ?, ?> triple = (Triple<?, ?, ?>) object;
		return Objects.equals(a, triple.getA()) && Objects.equals(b, triple.getB()) && Objects.equals(c, triple.getC());
	}

	@Override
	public int hashCode() {
		return Objects.hash(a, b, c);
	}

	@Override
	public final String toString() {
		return "{ " + a + ", " + b + ", " + c + " }";
	}
}