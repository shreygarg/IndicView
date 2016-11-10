Android Application which takes an input image and extracts text from the input image. Makes use of the Tess-Two API ( https://github.com/rmtheis/tess-two ) for tesseract and the CropImage Api ( https://github.com/chemalarrea/CropImage ) for cropping the camera Image. This code was written in eclipse earlier when NDK support on android-studio was limited. Now tess-two can be directly imported throught the gradle without using NDK.

The input image was preprocessed by using souvola binarization and tesseract was run on this binarized image.
