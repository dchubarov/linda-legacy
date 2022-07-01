
/*
 * TWOWLS.ORG PROPRIETARY/CONFIDENTIAL
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.twowls.lab.legacy.linde.render;

import org.twowls.lab.legacy.linde.producer.Interpreter;
import org.twowls.lab.legacy.linde.producer.ProducerException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author da
 */
public class PlanarTurtleRenderer implements Interpreter {

    private static final int CACHE_BUFFER_SIZE = 4096;
    private static final long CACHE_MAX_SIZE = 52428800; // 50M

    private static final char DRAW_FORWARD = 'F';
    private static final char DRAW_BACKWARD = 'B';
    private static final char MOVE_FORWARD = '>';
    private static final char MOVE_BACKWARD = '<';
    private static final char TURN_CCW = '+';
    private static final char TURN_CW = '-';
    private static final char SAVE_POSITION = '[';
    private static final char RESTORE_POSITION = ']';

    private Cache cache;

    private double initialAngle;
    private double turnAngle;
    private double grow;

    private LinkedList<Turtle> pstack = null;
    private double angle = 0.0;
    private double length = 0.0;
    private double posX = 0.0;
    private double posY = 0.0;
    private double extN = 0.0;
    private double extE = 0.0;
    private double extS = 0.0;
    private double extW = 0.0;

    private List<AngleDeviator> angleDeviators = null;

    public PlanarTurtleRenderer(double initialAngle,
            double turnAngle, double grow) {

        if (grow <= 0.0) {
            throw new IllegalArgumentException("PlanarTurtleRenderer(): " +
                    "grow must be positive.");
        }

        // Initialize control values
        this.initialAngle = initialAngle;
        this.turnAngle = turnAngle;
        this.grow = grow;

        // Initialize cache
        cache = new Cache(CACHE_BUFFER_SIZE, CACHE_MAX_SIZE);
    }

    public long getCacheMoves() {
        return cache.cacheMoves;
    }

    public void addAngleDeviator(AngleDeviator deviator) {
        if (deviator == null) {
            throw new IllegalArgumentException("deviator == null");
        }

        if (angleDeviators == null) {
            angleDeviators = new LinkedList<>();
        }

        angleDeviators.add(deviator);
    }

    public void start(int order) throws ProducerException {
        // Reset renderer cache
        cache.reset();

        // Initialize turtle context
        angle = initialAngle;
        length = 1.0;
        posX = 0.0;
        posY = 0.0;
        extN = 0.0;
        extE = 0.0;
        extS = 0.0;
        extW = 0.0;

        if (pstack != null) {
            pstack.clear();
        }
    }

    public void ascend(int depth) throws ProducerException {
        cache.writeBranch(depth);
        length *= grow;
    }

    public void descend(int depth) throws ProducerException {
        cache.writeBranch(depth);
        length /= grow;
    }

    public void interpret(char c) throws ProducerException {
        switch (c) {
            case TURN_CCW:
                angle -= turnAngle;
                if (angleDeviators != null) {
                    for (AngleDeviator deviator : angleDeviators) {
                        angle = deviator.deviate(angle, AngleDeviator.CCW);
                    }
                }
                break;

            case TURN_CW:
                angle += turnAngle;
                if (angleDeviators != null) {
                    for (AngleDeviator deviator : angleDeviators) {
                        angle = deviator.deviate(angle, AngleDeviator.CW);
                    }
                }
                break;

            case SAVE_POSITION:
                if (pstack == null) {
                    pstack = new LinkedList<>();
                }
                pstack.push(new Turtle(posX, posY, angle));
                break;

            case RESTORE_POSITION:
                if (pstack == null || pstack.size() < 1) {
                    throw new ProducerException("No position to restore.");
                }
                Turtle t = pstack.pop();
                cache.writeMove(t.x, t.y, false);
                angle = t.angle;
                posX = t.x;
                posY = t.y;
                break;

            case DRAW_FORWARD:
            case MOVE_FORWARD:
            case DRAW_BACKWARD:
            case MOVE_BACKWARD:
                // Calculate X and Y offsets relative to current position
                double angleRad = Math.toRadians(angle);
                double dx = length * Math.cos(angleRad);
                double dy = length * Math.sin(angleRad);

                // Reverse offsets if drawing/moving backwards
                if (c == DRAW_BACKWARD || c == MOVE_BACKWARD) {
                    dx = -dx;
                    dy = -dy;
                }

                // Move current position
                posX += dx;
                posY += dy;

                // Extent of fractal
                extN = Math.min(extN, posY);
                extE = Math.max(extE, posX);
                extW = Math.min(extW, posX);
                extS = Math.max(extS, posY);

                // Save current move in the cache
                cache.writeMove(posX, posY,
                        (c == DRAW_FORWARD || c == DRAW_BACKWARD));

                break;

            default:
        }
    }

    public void render(Surface surface) throws ProducerException {
        // Ensure all data is cached
        cache.flush();

        surface.setExtent(extN, extE, extS, extW);

        boolean done = false;
        double rx = 0.0;
        double ry = 0.0;

        do {
            int b = cache.readByte();
            if (b < 0) {
                done = true;
            }
            else {
                switch (b & 0xf0) {
                    case Cache.MOVE_START:
                        double dx = cache.readDouble();
                        double dy = cache.readDouble();
                        if ((b & 0x0f) != 0) {
                            surface.draw(rx, ry, dx, dy);
                        }
                        rx = dx;
                        ry = dy;
                        break;

                    case Cache.BRANCH_START:
                        cache.readByte();
                        cache.readByte();
                        break;

                    default:
                        throw new ProducerException("Invalid record.");
                }
            }
        } while (!done);
    }

    static class Turtle {
        double x;
        double y;
        double angle;

        public Turtle(double x, double y, double angle) {
            this.x = x;
            this.y = y;
            this.angle = angle;
        }
    }

    static class Cache {
        public static final int MOVE_START = 0xD0;
        public static final int BRANCH_START = 0xA0;

        private static final String CACHE_FILE_PREFIX = "fractal";

        private ByteArrayOutputStream baos;
        private int bufferSize;
        private long maxSize;

        private long cacheSize = 0L;
        private long cacheMoves = 0L;
        private File cacheFile = null;
        private OutputStream cacheOut = null;
        private InputStream cacheIn = null;

        public Cache(int bufferSize, long maxSize) {
            if (bufferSize < 256) {
                throw new IllegalArgumentException("PlanarTurtleInterpreter." +
                        "Cache(): Argument bufferSize is invalid.");
            }

            if (maxSize < bufferSize) {
                throw new IllegalArgumentException("PlanarTurtleInterpreter." +
                        "Cache(): Argument maxSize is invalid.");
            }

            baos = new ByteArrayOutputStream();
            this.bufferSize = bufferSize;
            this.maxSize = maxSize;
        }

        public void reset() throws ProducerException {
            if (cacheFile != null) {
                try {
                    cacheOut.close();
                }
                catch (IOException e) { }
                cacheOut = null;

                if (!cacheFile.delete()) {
                    // TODO log deletion error
                }
                cacheFile = null;
            }

            baos.reset();
            cacheSize = 0L;
            cacheMoves = 0L;
        }

        public void flush() throws ProducerException {
            if (baos.size() > 0 && cacheSize > bufferSize) {
                saveToDisk();
            }

            if (cacheIn != null) {
                try {
                    cacheIn.reset();
                }
                catch (IOException e) {
                    throw new ProducerException("I/O error reset input stream.");
                }
            }
        }

        public void writeMove(double x, double y, boolean draw)
                throws ProducerException {
            writeByte(MOVE_START | (draw ? 1 : 0));
            writeLong(Double.doubleToLongBits(x));
            writeLong(Double.doubleToLongBits(y));
            cacheMoves++;
        }

        public void writeBranch(int depth) throws ProducerException {
            writeByte(BRANCH_START);
            writeByte(depth & 0xff);
            writeByte((depth >> 8) & 0xff);
        }

        private void writeLong(long l) throws ProducerException {
            writeByte((int)(l & 0xff));
            writeByte((int)((l >> 8) & 0xff));
            writeByte((int)((l >> 16) & 0xff));
            writeByte((int)((l >> 24) & 0xff));
            writeByte((int)((l >> 32) & 0xff));
            writeByte((int)((l >> 40) & 0xff));
            writeByte((int)((l >> 48) & 0xff));
            writeByte((int)((l >> 56) & 0xff));
        }

        private void writeByte(int b) throws ProducerException {
            if (cacheSize >= maxSize) {
                reset();
                throw new ProducerException("Cache overflow.");
            }

            if (baos.size() >= bufferSize) {
                saveToDisk();
            }

            baos.write(b);
            cacheSize++;
        }

        private void saveToDisk() throws ProducerException {
            try {
                if (cacheFile == null) {
                    // Create temporary file
                    cacheFile = File.createTempFile(CACHE_FILE_PREFIX, null);
                    cacheFile.deleteOnExit();

                    // Open output stream
                    cacheOut = new FileOutputStream(cacheFile);
                }

                baos.writeTo(cacheOut);
                baos.reset();
            }
            catch (FileNotFoundException e) {
                throw new ProducerException("Could not create temp file");
            }
            catch (IOException e) {
                throw new ProducerException("I/O exception while writing to cache.");
            }
        }

        public int readByte() throws ProducerException {
            if (cacheIn == null) {
                if (baos.size() > 0) {
                    cacheIn = new ByteArrayInputStream(baos.toByteArray());
                }
                else {
                    if (cacheFile != null) {
                        try {
                            cacheOut.close();
                        }
                        catch (IOException e) { }
                        cacheOut = null;

                        try {
                            cacheIn = new BufferedInputStream(
                                    new FileInputStream(cacheFile));
                        }
                        catch (FileNotFoundException e) {
                            throw new ProducerException("Could not open cache file");
                        }
                    }
                }
            }

            int ret = -1;

            if (cacheIn != null) {
                try {
                    ret = cacheIn.read();
                }
                catch (IOException e) {
                    throw new ProducerException("I/O error reading cache file.");
                }
            }

            return ret;
        }

        public long readLong() throws ProducerException {
            long l = 0L;
            for (int i = 0; i < 64; i += 8) {
                int b = readByte();
                if (b == -1) {
                    throw new ProducerException("Unexpected end of file.");
                }

                l += (long) (b & 0xff) << i;
            }

            return l;
        }

        public double readDouble() throws ProducerException {
            return Double.longBitsToDouble(readLong());
        }
    }
}
