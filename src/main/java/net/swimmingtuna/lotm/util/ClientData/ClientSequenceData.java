package net.swimmingtuna.lotm.util.ClientData;

public class ClientSequenceData {
    private static int currentSequence;

    public static void setCurrentSequence(int sequence) {
        currentSequence = sequence;
    }

    public static int getCurrentSequence() {
        return currentSequence;
    }
}
