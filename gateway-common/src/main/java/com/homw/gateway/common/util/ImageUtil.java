package com.homw.gateway.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * @description 图片处理工具类
 * @author Hom
 * @version 1.0
 * @date 2019-11-01
 */
@SuppressWarnings("restriction")
public class ImageUtil {
	/**
	 * 按Base64位编码网络图片文件
	 * 
	 * @param imgUrl
	 * @return
	 */
	public static String encodeImgageToBase64(URL imgUrl) {
		try {
			encodeImgageToBase64(imgUrl.openStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 按Base64位编码图片文件
	 * 
	 * @param imgFile
	 * @return
	 */
	public static String encodeImgageToBase64(File imgFile) {
		try {
			return encodeImgageToBase64(new FileInputStream(imgFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 按Base64位编码图片流
	 * 
	 * @param in
	 * @return
	 */
	public static String encodeImgageToBase64(InputStream in) {
		if (in == null)
			return null;
		byte[] bytes = null;
		try {
			bytes = new byte[in.available()];
			in.read(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return new BASE64Encoder().encode(bytes);
	}

	/**
	 * 解码Base64位编码的图片，并保存到文件
	 * 
	 * @param base64Img
	 * @param imgFilePath
	 * @return
	 */
	public static void decodeBase64ToImage(String base64Img, String imgFilePath) {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(new File(imgFilePath));
			out.write(decodeBase64ToImage(base64Img));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 解码Base64位编码的图片
	 * 
	 * @param base64Img
	 * @return
	 */
	public static byte[] decodeBase64ToImage(String base64Img) {
		try {
			return new BASE64Decoder().decodeBuffer(base64Img);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}