package net.web.common;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

public class TestMultiPart {

	public static void main(String[] args) throws IOException {
		System.out.println("Staring");
		 final Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
		 
		 File f = new File("C:\\Users\\ADMIN\\Pictures\\Camera Roll\\WIN_20200414_10_55_52_Pro.jpg");
		 
		 System.out.println("File exist: " + f.exists());
		 
		    final FileDataBodyPart filePart = new FileDataBodyPart("file",f);
		    FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
		    final FormDataMultiPart multipart = (FormDataMultiPart) formDataMultiPart.field("foo", "bar").bodyPart(filePart);
		      
		    final WebTarget target = client.target("http://localhost:8080/bwservice/webapi/inv/file");
		    final Response response = target.request().post(Entity.entity(multipart, multipart.getMediaType()));
		     
		    System.out.println("Response: " + response);
		    //Use response object to verify upload success
		     
		    formDataMultiPart.close();
		    multipart.close();
	}

}
