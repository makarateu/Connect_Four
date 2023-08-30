//class for storing the minimax value and action
public class MinimaxInfo {
    int action;
    int value;

    public MinimaxInfo(int value, int action) {
        this.action = action;
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public int getAction() {
        return this.action;
    }
}