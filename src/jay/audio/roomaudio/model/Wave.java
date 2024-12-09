package jay.audio.roomaudio.model;


public class Wave {
    private double originX;
    private double originY;
    private long startTime; 
    private double initialAmplitude;
    private double alpha; // attenuation factor

    public Wave(double originX, double originY, long startTime, double initialAmplitude, double alpha) {
        this.originX = originX;
        this.originY = originY;
        this.startTime = startTime;
        this.initialAmplitude = initialAmplitude;
        this.alpha = alpha;
    }

    public double getRadius(long currentTime, double speedOfSound) {
        double elapsedSeconds = (currentTime - startTime) / 1_000_000_000.0;
        return speedOfSound * elapsedSeconds;
    }

    // Amplitude ~ A0 * exp(-alpha * r) / r
    public double getAmplitude(long currentTime, double speedOfSound) {
        double r = getRadius(currentTime, speedOfSound);
        if (r < 1.0) r = 1.0; // avoid division by zero
        return initialAmplitude * Math.exp(-alpha * r) / r;
    }

    public double getOriginX() {
        return originX;
    }

    public double getOriginY() {
        return originY;
    }

    public long getStartTime() {
        return startTime;
    }

    public double getInitialAmplitude() {
        return initialAmplitude;
    }

    public double getAlpha() {
        return alpha;
    }
}