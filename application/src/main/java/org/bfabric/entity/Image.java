package org.bfabric.entity;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.util.FileHelper;
import org.hibernate.annotations.DynamicUpdate;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.CroppedImage;
import org.primefaces.model.DefaultStreamedContent;

@Entity
@DynamicUpdate
@XmlRootElement
public class Image extends AbstractEntity {

    private static final long serialVersionUID = 1;

    private static final Logger logger = Logger.getLogger(Image.class.getName());

    @Column(columnDefinition = "BYTEA")
    @XmlElement
    private byte[] content;

    @Transient
    private Image cropped;

    @Transient
    private CroppedImage croppedImage;

    public Image() {
    }

    public Image(byte[] content) {
        super();
        setContent(content);
    }

    public void crop() {
        if (getCroppedImage() != null) {
            InputStream byteArrayInputStream = new ByteArrayInputStream(getContent());
            BufferedImage bufferedImage;
            try {
                bufferedImage = ImageIO.read(byteArrayInputStream);
                if (bufferedImage != null) {
                    BufferedImage croppedBufferedImage = bufferedImage.getSubimage(getCroppedImage().getLeft(), getCroppedImage().getTop(), getCroppedImage().getWidth(), getCroppedImage().getHeight());
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ImageIO.write(croppedBufferedImage, "jpg", byteArrayOutputStream);
                    byte[] bytes = byteArrayOutputStream.toByteArray();
                    setCropped(new Image(bytes));
                }
            } catch (IOException e) {
                logger.warning(e.getMessage());
            }
        }
    }

    public byte[] getContent() {
        return content != null ? content.clone() : null;
    }

    public Image getCropped() {
        return cropped;
    }

    public CroppedImage getCroppedImage() {
        return croppedImage;
    }

    public DefaultStreamedContent getStreamedContent() {
        return FileHelper.getDefaultStreamedContent(new ByteArrayInputStream(getContent()), "image/*", getIdString());
    }

    public void setContent(byte[] content) {
        this.content = content != null ? content.clone() : null;
        getImageCropperHelper().setImage(this);
        setCropped(null);
    }

    public void setCropped(Image cropped) {
        this.cropped = cropped;
    }

    public void setCroppedImage(CroppedImage croppedImage) {
        this.croppedImage = croppedImage;
    }

    public void takeOverCropped() {
        if (getCropped() != null && getCropped().getContent() != null) {
            setContent(getCropped().getContent());
        }
    }

    /**
     * Listener for ImageUploadEvent.
     *
     * @param event the event
     */
    public void uploadListener(FileUploadEvent event) {
        setContent(getFileUploadHelper().getImageUpload(event));
    }
}
