/**
 * Program that uses seam-carving resizing technique on an image
 *
 * @author Bryce Sulin
 * @version February 2018
 */
public class SeamCarver {

    private EasyBufferedImage picture;
    private int height;
    private int width;
    private double[] energy;
    private double[] distTo;
    private int[] edgeTo;

    // Create a seam carver object based on the given picture
    public SeamCarver(EasyBufferedImage picture) {
        if (picture == null) {
            throw new NullPointerException();
        }
        height = picture.getHeight();
        width = picture.getWidth();
        this.picture = picture;
    }

    // Energy of pixel at column x and row y
    public double getEnergy(int row, int col) {
        double energy = 0.0;

        if (row < 0 || row >= width) {
            throw new IndexOutOfBoundsException();
        }

        if (col < 0 || col >= height) {
            throw new IndexOutOfBoundsException();
        }

        for (col = 0; col < height; col++) {
            for (row = 0; row < width; row++) {
                int row1Pixel;
                int row2Pixel;
                int col1Pixel;
                int col2Pixel;

                if (row == 0) {
                    // leftmost column
                    row1Pixel = picture.getRGB(row, col);
                    row2Pixel = picture.getRGB(row + 1, col);
                } else if (row == width - 1) {
                    // rightmost column
                    row1Pixel = picture.getRGB(row - 1, col);
                    row2Pixel = picture.getRGB(row, col);
                } else {
                    // middle columns
                    row1Pixel = picture.getRGB(row - 1, col);
                    row2Pixel = picture.getRGB(row + 1, col);
                }

                if (col == 0) {
                    // bottom row
                    col1Pixel = picture.getRGB(row, col);
                    col2Pixel = picture.getRGB(row, col + 1);
                } else if (col == height - 1) {
                    // top row
                    col1Pixel = picture.getRGB(row, col - 1);
                    col2Pixel = picture.getRGB(row, col);
                } else {
                    // middle rows
                    col1Pixel = picture.getRGB(row, col - 1);
                    col2Pixel = picture.getRGB(row, col + 1);
                }

                int rowRed = Math.abs(((row1Pixel & 0x00ff0000) >> 16) - ((row2Pixel & 0x00ff0000) >> 16));
                int rowGreen = Math.abs(((row1Pixel & 0x0000ff00) >> 8) - ((row2Pixel & 0x0000ff00) >> 8));
                int rowBlue = Math.abs((row1Pixel & 0x000000ff) - (row2Pixel & 0x000000ff));

                int colRed = Math.abs(((col1Pixel & 0x00ff0000) >> 16) - ((col2Pixel & 0x00ff0000) >> 16));
                int colGreen = Math.abs(((col1Pixel & 0x0000ff00) >> 8) - ((col2Pixel & 0x0000ff00) >> 8));
                int colBlue = Math.abs((col1Pixel & 0x000000ff) - (col2Pixel & 0x000000ff));

                energy = rowRed + rowGreen + rowBlue + colRed + colGreen + colBlue;
            }
        }
        return energy;
    }

    private int position(int col, int row) {
        return width * row + col;
    }

    private int positionRow(int position) {
        return position / width;
    }

    private int positionColumn(int position) {
        return position % width;
    }

    private void relax(int from, int to) {
        if (distTo[to] > distTo[from] + energy[to]) {
            distTo[to] = distTo[from] + energy[to];
            edgeTo[to] = from;
        }
    }

    // Sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        int size = width * height;

        energy = new double[size];
        distTo = new double[size];
        edgeTo = new int[size];
        int p;

        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                p = position(col, row);

                if (row == 0) {
                    distTo[p] = 0;
                } else {
                    distTo[p] = Double.POSITIVE_INFINITY;
                }

                energy[p] = getEnergy(col, row);
                edgeTo[p] = -1;
            }
        }

        for (int row = 0; row < height - 1; row++) {
            for (int col = 0; col < width; col++) {
                p = position(col, row);

                if (col - 1 >= 0) {
                    relax(p, position(col - 1, row + 1));
                }

                relax(p, position(col, row + 1));

                if (col + 1 < width) {
                    relax(p, position(col + 1, row + 1));
                }
            }
        }

        double min = Double.POSITIVE_INFINITY;
        int end = 0;

        for (int col = 0; col < width; col++) {
            if (distTo[position(col, height - 1)] < min) {
                min = distTo[position(col, height - 1)];
                end = position(col, height - 1);
            }
        }
        return verticalSeam(end);
    }

    private int[] verticalSeam(int end) {
        int[] result = new int[height];
        int tmp = end;

        while (tmp >= 0) {
            result[positionRow(tmp)] = positionColumn(tmp);
            tmp = edgeTo[tmp];
        }
        return result;
    }

    // Sequence of indices for a horizontal seam
    public int[] findHorizontalSeam() {
        int size = width * height;

        energy = new double[size];
        distTo = new double[size];
        edgeTo = new int[size];
        int p;

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                p = position(col, row);

                if (col == 0) {
                    distTo[p] = 0;
                } else {
                    distTo[p] = Double.POSITIVE_INFINITY;
                }

                energy[p] = getEnergy(col, row);
                edgeTo[p] = -1;
            }
        }

        for (int col = 0; col < width - 1; col++) {
            for (int row = 0; row < height; row++) {
                p = position(col, row);

                if (row - 1 >= 0) {
                    relax(p, position(col + 1, row - 1));
                }

                relax(p, position(col + 1, row));

                if (row + 1 < height) {
                    relax(p, position(col + 1, row + 1));
                }
            }
        }

        double min = Double.POSITIVE_INFINITY;
        int end = 0;

        for (int row = 0; row < height; row++) {
            if (distTo[position(width - 1, row)] < min) {
                min = distTo[position(width - 1, row)];
                end = position(width - 1, row);
            }
        }

        return horizontalSeam(end);
    }

    private int[] horizontalSeam(int end) {
        int[] result = new int[width];
        int tmp = end;

        while (tmp >= 0) {
            result[positionColumn(tmp)] = positionRow(tmp);
            tmp = edgeTo[tmp];
        }
        return result;
    }

    // Find and remove vertical seam from the picture
    public void findAndRemoveVerticalSeam() {
        if (findVerticalSeam() == null) {
            throw new NullPointerException();
        }

        if (findVerticalSeam().length != height) {
            throw new IllegalArgumentException();
        }

        EasyBufferedImage original = this.picture;
        EasyBufferedImage carved = original;

        for (int h = 0; h < carved.getHeight(); h++) {
            for (int w = 0; w < findVerticalSeam()[h]; w++) {
                carved.setRGB(w, h, original.getRGB(w, h));
            }
            for (int w = findVerticalSeam()[h]; w < carved.getWidth(); w++) {
                carved.setRGB(w, h, original.getRGB(w + 1, h));
            }
        }
        this.picture = carved;
    }

    // Find and remove horizontal seam from the picture
    public void findAndRemoveHorizontalSeam() {
        EasyBufferedImage original = this.picture;
        EasyBufferedImage transpose = original;

        for (int w = 0; w < transpose.getWidth(); w++) {
            for (int h = 0; h < transpose.getHeight(); h++) {
                transpose.setRGB(w, h, original.getRGB(h, w));
            }
        }

        this.picture = transpose;
        transpose = null;
        original = null;

        findAndRemoveVerticalSeam();

        original = picture;
        transpose = original;

        for (int w = 0; w < transpose.getWidth(); w++) {
            for (int h = 0; h < transpose.getHeight(); h++) {
                transpose.setRGB(w, h, original.getRGB(h, w));
            }
        }

        this.picture = transpose;
        transpose = null;
        original = null;
    }
}