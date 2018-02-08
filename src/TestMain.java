/**
 * Program to illustrate treating an EasyBufferedImage as a 3D
 * array of pixels
 *
 * @author Stuart Hansen
 * @version January 2018
 */
public class TestMain {
   public static void main (String [] args) throws Exception {

       // Note the use of the createImage method rather than a constructor
       EasyBufferedImage image = EasyBufferedImage.createImage("smallturkey.png");
       image.show("Original Image");

       // Get the image as a 3D array of pixels
       int [][][] pixels = image.getPixels3D();

       // Leave the green and kill the red and blue
       for (int i=0; i<image.getHeight(); i++)
           for (int j=0; j<image.getWidth(); j++) {
               // Kill the red and blue values
               pixels[i][j][0] = 0;
               pixels[i][j][2] = 0;
           }
       // Create an image with the new pixel values
       EasyBufferedImage newImage = EasyBufferedImage.createImage(pixels);
       newImage.show("New Image 1");


       // Draw a red circle 50 pixels in radius
       //Find the center of the image
       int rc = image.getHeight()/2;
       int cc = image.getWidth()/2;
       for (double d=0; d<2*Math.PI; d+=0.001) {
           int row = rc + (int) (50 * Math.sin(d));
           int col = cc + (int) (50 * Math.cos(d));
           image.setRGB(col, row, 255<<16);

       }
       //newImage = EasyBufferedImage.createImage(pixels);
       image.show("Red Circle");

   }

}
