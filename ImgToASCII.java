import java.util.Scanner;
import java.io.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public final class ImgToASCII {
    
    public static final char[] SYM_LIST = { ' ', '.', ':', '-', '=', '+', '*', '#', '%', '@' };
    public static final char[] INV_LIST = { '@', '%', '#', '*', '+', '=', '-', ':', '.', ' ' };
    public static final int RGB_MAX = 255;
    public static final double LUMA_MAX = 1.0;
    public static final double W_H_RATIO = 2;
    public static final char INVERTED = 'y'; // 'y' will evaluate to true
    public static final String IMG_EXT = ".png";
    public static final int CONSOLE_WIDTH = 80;
    public static final int CONSOLE_HEIGHT = 24;
    
    private static String fp = "default.jpg";
    private static int y_sam = 0;
    private static int x_sam = 2;
    private static boolean inverted = false;
    private static String imgOut = "ImageOut";
    
    /** Main method. Outsources all of the work to other methods. */
    public static void main(String[] args) {
        // -------- Input --------
        if ( args.length == 0 ) {
            // Get input via keyboard input
            getInput();
        } else if ( args.length == 3 ) {
            // Input via command line
            fp = args[0];
            x_sam = Integer.parseInt( args[1] );
            imgOut = args[2];
        } else if ( args.length == 4 ) {
            // input via command line ( with inverted characters )
            fp = args[0];
            x_sam = Integer.parseInt( args[1] );
            imgOut = args[2];
            inverted = ( args[3].charAt(0) == INVERTED );
        } else {
            System.out.println( "usage: java JpegToASCII path_to_file samples output_file(w/o_ext) [opt: inverted (y/n)]");
        }
        
        // Append the extension
        imgOut += IMG_EXT;
        
        // -------- Logic --------
        
        // convert the image
        String[] imageText = convertASCII();
        
        // Write the image
        writeImageFromText( imageText );
        
        // Re-render for terminal output
        System.out.println( terminalASCII() );
        
        // System.out.println( imageText );
    }
    
    /** Writes an image from a String. The font is consolas. */
    private static void writeImageFromText( String[] text ) {
        /*
         Because font metrics is based on a graphics context, we need to create
         a small, temporary image so we can ascertain the width and height
         of the final image
         */
        /*
         Code based on post from Stack Overflow:
         http://stackoverflow.com/questions/18800717/convert-text-content-to-image
         */
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        Font font = new Font("Consolas", Font.PLAIN, 12);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(text[0]);
        int height = fm.getHeight() * text.length;
        g2d.dispose();
        
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setFont(font);
        fm = g2d.getFontMetrics();
        g2d.setColor(Color.BLACK);
        
        // Keep track of y
        float y = fm.getAscent();
        for ( int i = 0; i < text.length; i++ ) {
            g2d.drawString(text[i], 0, y);
            // update y
            y += fm.getAscent() + fm.getDescent();// + fm.getLeading();
        }
        g2d.dispose();
        try {
            //            ImageIO.write(img, "png", new File("Text.png"));
            ImageIO.write(img, "png", new File("" + imgOut));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
    
    /**
     Converts an image to ASCII characters based on the brightness of pixels.
     The resulting characters are arbitrary of line shape for the original image.
     */
    private static String[] convertASCII() {
        // Calculate the y sampling rate
        y_sam = (int) ( (double) x_sam * W_H_RATIO );
        
        // Build the string to convert to img
        String toReturn[] = { "" };
        
        try {
            // Input the image file
            File file = new File( fp );
            BufferedImage image = ImageIO.read(file);
            
            // We want the array we are returning to be just big enough to hold all of the lines
            toReturn = new String[ image.getHeight() / y_sam ];
            
            // iterate through image
            // Iterate for every row
            for ( int i = 0; i < toReturn.length; i++ ) {
                // Get the y value
                int y = i * y_sam;
                
                // Init to ""
                toReturn[i] = "";
                
                // Iterate for every column
                for ( int x = 0; x < image.getWidth(); x += x_sam ) {
                    // Getting pixel color by position x and y
                    int clr =  image.getRGB(x,y);
                    int red   = (clr & 0x00ff0000) >> 16;
                    int green = (clr & 0x0000ff00) >> 8;
                    int blue  =  clr & 0x000000ff;
                    
                    // get the symbol
                    char sym = symbol( red, green, blue );
                    
                    // append the symbol
                    toReturn[ i ] += sym;
                }
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        
        // Return the String
        return toReturn;
    }
    
    /** Returns a String for Terminal Output. */
    private static String terminalASCII() {
        // Build the string to convert to img
        String toReturn = "";
        
        try {
            // Input the image file
            File file = new File( fp );
            BufferedImage image = ImageIO.read(file);
            
            // Determine the constraining dimension for the image
            if ( image.getWidth() > image.getHeight() ) {
                // Calculate x_sam
                x_sam = image.getWidth() / CONSOLE_WIDTH;
                // Calculate the y sampling rate
                y_sam = (int) ( (double) x_sam * W_H_RATIO );
            } else {
                // Calc y_sam
                y_sam = image.getHeight() / CONSOLE_HEIGHT;
                // Calc x_sam
                x_sam = (int) ( (double) y_sam / W_H_RATIO );
            }
            
            // iterate through image
            // Iterate for every row
            for ( int y = 0; y < image.getHeight(); y += y_sam ) {
                // Iterate for every column
                for ( int x = 0; x < image.getWidth(); x += x_sam ) {
                    // Getting pixel color by position x and y
                    int clr =  image.getRGB(x,y);
                    int red   = (clr & 0x00ff0000) >> 16;
                    int green = (clr & 0x0000ff00) >> 8;
                    int blue  =  clr & 0x000000ff;
                    
                    // get the symbol
                    char sym = symbol( red, green, blue );
                    
                    // append the symbol
                    toReturn += sym;
                }
                // Add new line
                toReturn += "\n";
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        
        // Return the String
        return toReturn;
    }
    
    /** Returns the symbol for the pixel. */
    private static char symbol( int r, int g, int b ) {
        double rpart = ( (double)r / RGB_MAX ) * 0.3;
        double gpart = ( (double)g / RGB_MAX ) * 0.59;
        double bpart = ( (double)b / RGB_MAX ) * 0.11;
        
        // Add the components to get the brightness
        double luma = rpart + gpart + bpart;
        
        // get the symbol
        int i = 0;
        if ( luma >= 0.10 )
            i = (int)( ( luma * (double) SYM_LIST.length ) - 0.25 );
        
        // System.out.printf( "luma: %4.1f i: %1d   ", luma, i );
        
        // Return the symbol
        return (inverted ? INV_LIST[i] : SYM_LIST[i] );
    }
    
    /** Takes input from the keyboard. see below for how it works. */
    public static void getInput() {
        Scanner k = new Scanner( System.in );
        String in = "";
        
        System.out.println( "--------------------------------------------------------------------------------" );
        System.out.println( "                      \"Convert Image File to ASCII Image\"                       " );
        System.out.println( "--------------------------------------------------------------------------------" );
        
        System.out.print( "Path/File.ext: " );
        fp = k.nextLine();
        
        System.out.print( "Sample 1 px every: " );
        x_sam = k.nextInt();
        
        k.nextLine();
        System.out.print( "File out (w/o ext): " );
        imgOut = k.nextLine();
        
        System.out.print( "Invert symbols (y/n): " );
        String inv = k.nextLine();
        inverted = ( inv.charAt(0) == INVERTED );
        
    }
}
