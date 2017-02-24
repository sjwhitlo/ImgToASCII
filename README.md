# ImgToASCII

Takes an image and converts to ASCII characters. The final text is rendered to a png.

Runs from the command line via arguments or keyboard input.

## Supported Input Files
- .png
- .jpg / .jpeg
- .gif

## Command Line Arguments
- path to file
- samples (int)
  - Where samples are every _ith_ pixel
- output file name (without the extension)
- optional: invert symbols (y/n)


# ConvertFilesToASCII

Takes a directory, and calls ImgToASCII for all files in the directory

Runs from the command line via arguments or keyboard input.

## Command Line Arguments
- path to file
- samples (int)
  - Where samples are every _ith_ pixel
- frame rate (int)
  - Where every _ith_ frame is rendered
- batch output file name (without the extension)
- optional: invert symbols (y/n)
