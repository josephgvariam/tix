package com.dubaidrums.tickets.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/download/**")
@Controller
public class DownloadController {
	
	Logger log = LogManager.getLogger(DownloadController.class);
	
	@Autowired
	ServletContext servletContext;
	
	@RequestMapping("/ticketsdb")
	@ResponseBody
	public byte[] getImage(HttpServletResponse response) throws IOException {
	    File imageFile = new File("/var/tomcat/ticketsDB/ticketsDB.script");
	    byte[] bytes = org.springframework.util.FileCopyUtils.copyToByteArray(imageFile);

	    response.setHeader("Content-Disposition", "attachment; filename=\"" + imageFile.getName() + "\"");
	    response.setContentLength(bytes.length);
	    response.setContentType("text/plain");

	    return bytes;
	}
	
	
	@RequestMapping("/tix")
	@ResponseBody
	public byte[] getTix(@RequestParam(value = "eventname", required = true) String eventName, HttpServletResponse response) throws IOException {
		log.info("Building TIX app...");
		
		try{
		String path = servletContext.getRealPath("/");
		String tixPath = path+"WEB-INF/tix/tix/";
		String propPath = tixPath+"webapps/tix/WEB-INF/classes/app.properties";
		
		FileInputStream in = new FileInputStream(propPath);
        Properties props = new Properties();
        props.load(in);
        in.close();

        FileOutputStream out = new FileOutputStream(propPath);
        props.setProperty("eventname", eventName);
        props.store(out, null);
        out.close();
		
        String tmpFile = "tix-"+new Date().getTime();
        String tmpDir = "/tmp/"+tmpFile;
//        FileUtils.copyDirectory(new File(tixPath), new File(tmpDir), true);
//        FileUtils.copyDirectory(new File(path+"../../../ticketsDB"), new File(tmpDir+"/ticketsDB"), true);
        
        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec("cp -aR "+tixPath+" "+tmpDir);
        pr.waitFor();
        //localhost pr = rt.exec("cp -aR "+path+"../../../ticketsDB "+tmpDir+"/ticketsDB");
        pr = rt.exec("cp -aR "+path+"../../ticketsDB "+tmpDir+"/ticketsDB");
        pr.waitFor();
        pr = rt.exec("tar -czf "+tmpFile+".tar.gz "+tmpFile,null,new File("/tmp"));
        pr.waitFor();
		
	    File tarFile = new File(tmpDir+".tar.gz");
	    byte[] bytes = org.springframework.util.FileCopyUtils.copyToByteArray(tarFile);

	    response.setHeader("Content-Disposition", "attachment; filename=\"" + tarFile.getName() + "\"");
	    response.setContentLength(bytes.length);
	    response.setContentType("text/plain");

	    return bytes;
		}catch(Exception e){
			log.error("could not build tix app!", e);
			return null;
		}
	}

}
