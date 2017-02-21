import java.io.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public final class JpegToASCII {
    
    public static final char[] SYM_LIST = { ' ', '.', ':', '-', '=', '+', '*', '#', '%', '@' };
    public static final char[] INV_LIST = { '@', '%', '#', '*', '+', '=', '-', ':', '.', ' ' };
    public static final int RGB_MAX = 255;
    public static final double LUMA_MAX = 1.0;
    public static final double W_H_RATIO = 2;
    
    public static void main(String[] args) {
        try {
            String fp = "Wolf.jpg";
            int y_sam;
            int x_sam = 9;
            char inverted = 'n';
            
            if ( args.length == 2 ) {
                fp = args[0];
                x_sam = Integer.parseInt( args[1] );
            } else if ( args.length == 3 ) {
                fp = args[0];
                x_sam = Integer.parseInt( args[1] );
                inverted = args[2].charAt(0);
            } else if ( args.length != 1 && args.length != 3 ) {
                System.out.println( "usage: java JpegToASCII [path_to_file] [width sample (int)] [opt: inverted (y/n)]");
            }
            
            y_sam = (int) ( (double) x_sam * W_H_RATIO );
            
            // Input the image file
            File file = new File( fp );
            BufferedImage image = ImageIO.read(file);
            
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
                    
                    char sym = symbol( red, green, blue, (inverted == 'y') );
                    //                System.out.printf("r: %3d, g: %3d, b: %3d, s: \'%3c\'\n", red, green, blue, sym);
                    
                    System.out.print(sym);
                }
                System.out.println();
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }
    
    private static char symbol( int r, int g, int b, boolean inv ) {
        double rpart = ( (double)r / RGB_MAX ) * 0.3;
        double gpart = ( (double)g / RGB_MAX ) * 0.59;
        double bpart = ( (double)b / RGB_MAX ) * 0.11;
        
        double luma = rpart + gpart + bpart;
        
//        System.out.printf("   r: %3f, g: %3f, b: %3f, l: %3f\n", rpart, gpart, bpart, luma);
        
        for ( int i = 0; i < SYM_LIST.length; i++ ) {
            if ( luma < (i+1) * LUMA_MAX / SYM_LIST.length ) {
                return (inv ? INV_LIST[i] : SYM_LIST[i] );
            }
        }
        
        // default return
        return 'X';
    }
}
