

/**
 *  Copyright 2012 Charles du Jeu
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 *  This file is part of the AjaXplorer Java Client
 *  More info on http://ajaxplorer.info/
 */
package pydio.sdk.java.http;



import org.apache.http.entity.mime.content.FileBody;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * 
 * @author pydio
 *
 */

public class UploadFileBody extends FileBody {

	private String customFileName;
	private int chunkSize = 0;
	private int chunkIndex = 0;
	private int totalChunks;
	private int lastChunkSize;
	private int bufsize = 0;


    private CountingMultipartRequestEntity.ProgressListener progressListener;

    public CountingMultipartRequestEntity.ProgressListener listener(){
        return progressListener;
    }
    public void setListener(CountingMultipartRequestEntity.ProgressListener listener){
        progressListener = listener;
    }
	
	public UploadFileBody(File file, String fileName) {
		super(file);
		customFileName = fileName;
		bufsize = 16384;
	}
	
	public void chunkIntoPieces(int chunkSize){
		this.chunkSize = chunkSize;			
		totalChunks = (int) Math.ceil( (float)this.getFile().length() / (float)this.chunkSize );
		if( ((float)this.getFile().length() % (float)this.chunkSize ) == 0 ){
			lastChunkSize = chunkSize;
		}else{
			lastChunkSize = (int) getFile().length() - (this.chunkSize*(totalChunks-1));
		}
		
		if (totalChunks > 1 && bufsize > chunkSize){			
			bufsize = chunkSize;
		}
	}
	public int getCurrentIndex(){
		return this.chunkIndex;
	}
	public int getTotalChunks(){
		return this.totalChunks;
	}
	public void resetChunkIndex(){
		chunkIndex = 0;
	}
	public boolean isChunked(){
		return this.chunkSize > 0;
	}
	public boolean allChunksUploaded(){
		return this.chunkIndex >= totalChunks;
	}
	public String getRootFilename(){
		return customFileName;
	}
	
	@Override
	public String getFilename(){
		if(this.chunkSize > 0){
			if(this.chunkIndex == 0) return customFileName;
			else return customFileName + "-" + this.chunkIndex;				
		}
		return customFileName;
	}
	
	public long getContentLength(){
		if(this.chunkSize > 0) {
			if(this.chunkIndex == (totalChunks - 1)){
				return (long) lastChunkSize;
			}else{
				return (long) this.chunkSize;
			}
		}
		else return getFile().length();
	}
	
	public void writeTo(OutputStream out){
		
		InputStream in;
		//int bufsize = Integer.parseInt(StateHolder.getInstance().getLocalConfig(Pydio.LOCAL_CONFIG_BUFFER_SIZE));

		
		try {
			if(this.chunkSize > 0){
				RandomAccessFile raf = new RandomAccessFile(getFile(), "r");
				int start = chunkIndex * this.chunkSize;
				
				int count = 0;
				int limit = chunkSize;
				byte[] buffer = new byte[bufsize];
				
				if(chunkIndex == (totalChunks -1)){
					limit = lastChunkSize;
				}
				
				raf.seek(start);
				
				while(count < limit){
					
					if(count + bufsize > limit){
						if (count == 0){
							bufsize = limit;
						}else{
							bufsize = limit - count;
						}
					}
									
					raf.read(buffer, 0, bufsize);
					out.write(buffer, 0, bufsize);		
					count += bufsize;
				}				
				raf.close();			
			}else{
				in = new FileInputStream(getFile());
				byte[] buf = new byte[bufsize];
				int len;
				while ((len = in.read(buf)) > 0){
					out.write(buf, 0, len);
				}
				in.close();
			}
			this.chunkIndex++;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

}
