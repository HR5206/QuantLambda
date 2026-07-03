package ast;

public interface Node {
    <R> R accept(Visitor<R> visitor);
}
