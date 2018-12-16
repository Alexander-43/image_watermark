package ru.aeltsov.wateredimage;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.ImageIcon;

public class WIProcessor {
	
	public static final String PATH_SEPARATOR = System.getProperty("file.separator");
	
	String sourcePath = "";
	String targetPath = "";
	String waterText = "";
	Float compressRation = 0.0f;
	
	public WIProcessor() {
		// TODO Auto-generated constructor stub
	}
	
	public WIProcessor(String sourcePath, String targetPath, String waterText, Float compressRation){
		this.setSourcePath(sourcePath);
		this.setTargetPath(targetPath);
		this.setWaterText(waterText);
		this.setCompressRation(compressRation);
	}

	public void process() throws Exception{
		if (getSourcePath().isEmpty() || getTargetPath().isEmpty() || getWaterText().isEmpty()){
			throw new Exception("Обязательные параметры не заданы");
		}
		
		if (!(new File(getTargetPath())).isDirectory()){
			throw new Exception("Папка назначения должна существовать");
		}
		
		for (File file:getFileListFromPath(getSourcePath())){
			if (file.getName().indexOf(".jpg") > -1 || file.getName().indexOf(".JPG") > -1){
				File compFile = compress(file, "", getCompressRation());
				addWaterMarkJPG(compFile);
				compress(file, "tmb_", getCompressRation()/2);
			}
		}
		
	}
	
	private File compress(File input, String namePrefix, Float ratio) throws IOException{
	    BufferedImage image = ImageIO.read(input);

	    File compressedImageFile = new File(getTargetPath()+ PATH_SEPARATOR + namePrefix + input.getName());
	    OutputStream os = new FileOutputStream(compressedImageFile);

	    Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
	    ImageWriter writer = (ImageWriter) writers.next();
	    ImageOutputStream ios = ImageIO.createImageOutputStream(os);
	    writer.setOutput(ios);

	    ImageWriteParam param = writer.getDefaultWriteParam();
	    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
	    param.setCompressionQuality(ratio);  // Change the quality value you prefer
	    writer.write(null, new IIOImage(image, null, null), param);

	    os.close();
	    ios.close();
	    writer.dispose();
	    return compressedImageFile;
	}
	
	private void addWaterMarkJPG(File origFile){
        File newFile = new File(getTargetPath() + PATH_SEPARATOR + origFile.getName());
        try {
        		BufferedImage bufferedImage = ImageIO.read(origFile);
        		watermark(bufferedImage, getWaterText());
              ImageIO.write(bufferedImage, "jpg", newFile);
        } catch (IOException e) {
              e.printStackTrace();
        }

        System.out.println((new Date()).getTime() + newFile.getPath() + " created successfully!");
	}
	
	private void watermark(BufferedImage original, String watermarkText) {
        // create graphics context and enable anti-aliasing
        Graphics2D g2d = original.createGraphics();
        g2d.scale(1, 1);
        g2d.addRenderingHints(
                new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                                   RenderingHints.VALUE_ANTIALIAS_ON));
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                             RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // create watermark text shape for rendering
        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, original.getHeight()/20);
        GlyphVector fontGV = font.createGlyphVector(g2d.getFontRenderContext(), watermarkText);
        Rectangle size = fontGV.getPixelBounds(g2d.getFontRenderContext(), 0, 0);
        Shape textShape = fontGV.getOutline();
        double textWidth = size.getWidth();
        double textHeight = size.getHeight();
        AffineTransform rotate45 = AffineTransform.getRotateInstance(Math.PI / 4d);
        Shape rotatedText = rotate45.createTransformedShape(textShape);

        // use a gradient that repeats 4 times
        g2d.setPaint(new GradientPaint(0, 0,
                            new Color(0f, 0f, 0f, 0.1f),
                            original.getWidth() / 2, original.getHeight() / 2,          
                            new Color(0f, 0f, 0f, 0.1f)
                            //new Color(1f, 1f, 1f, 0.1f)
        ));
        g2d.setStroke(new BasicStroke(0.5f));

        // step in y direction is calc'ed using pythagoras + 5 pixel padding
        double yStep = Math.sqrt(textWidth * textWidth / 2) + 5;

        // step over image rendering watermark text
        for (double x = -textHeight * 3; x < original.getWidth(); x += (textHeight * 3)) {
            double y = -yStep;
            for (; y < original.getHeight(); y += yStep) {
                g2d.draw(rotatedText);
                g2d.fill(rotatedText);
                g2d.translate(0, yStep);
            }
            g2d.translate(textHeight * 3, -(y + yStep));
        }
    }
	
	private List<File> getFileListFromPath(String path){
		try {
			if ((new File(path)).isDirectory()){
				return Files.walk(Paths.get(path)).filter(Files::isRegularFile).map(Path::toFile).collect(Collectors.toList());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ArrayList<File>();
	}
	
	public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	public String getTargetPath() {
		return targetPath;
	}

	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
	}

	public String getWaterText() {
		return waterText;
	}

	public void setWaterText(String waterText) {
		this.waterText = waterText;
	}

	public Float getCompressRation() {
		return compressRation;
	}

	public void setCompressRation(Float compressRation) {
		this.compressRation = compressRation;
	}
}
