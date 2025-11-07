package com.test.cbsSign;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.sap.aii.mapping.api.*;

import org.json.JSONArray;

//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;

//import org.w3c.dom.Document;
//import org.xml.sax.SAXException;
import org.json.JSONObject;
import org.json.XML;

import com.sap.aii.mapping.api.AbstractTrace;

//import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Enumeration;

import org.bouncycastle.asn1.*;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;
import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Base64;

public class CBSSign extends AbstractTransformation {
	
	public static int PRETTY_PRINT_INDENT_FACTOR = 4;
	private static final String STD_NAME = "sm2p256v1";
	
	 /* 企业私钥（加密）
     */
	//static final String signEncryptionPrivateKey = "347bf1f723af15f31de13819e0e28025927e080e69060afa5d7f7097ef2edc55";
	
	@Override
	public void transform(TransformationInput transformationInput,TransformationOutput transformationOutput ) throws StreamTransformationException
	{
		
		long timestamp = System.currentTimeMillis();
	    String sourcexml = "";
	    String line = "";	
		String token = "";
	    String encryption_private_key = "";
		String requestData = "";
		Object value ;
		String add_square = "";
		String body_encryption_key = "";
		
		AbstractTrace trace = getTrace();
		DynamicConfiguration conf = (DynamicConfiguration) transformationInput.getDynamicConfiguration();
		//获取token
		
		DynamicConfigurationKey TOKEN = DynamicConfigurationKey.create("http:/"+"/sap.com/xi/XI/System/REST","Token");

		
		//获取私钥
		//DynamicConfigurationKey EncryptionPrivateKey = DynamicConfigurationKey.create("http:/"+"/sap.com/xi/XI/System/REST","EncryptionPrivateKey");
		//encryption_private_key = conf.get(EncryptionPrivateKey);		
		//trace.addInfo("key:" + encryption_private_key);
		
	    //写入时间戳	    
		DynamicConfigurationKey TIMESTAMP = DynamicConfigurationKey.create("http:/"+"/sap.com/xi/XI/System/REST","X-MBCLOUD-TIMESTAMP");	     
	    conf.put(TIMESTAMP, Long.toString(timestamp));		

	    try 
	    {
	    	//xml转json
		    InputStream inputstream = transformationInput.getInputPayload().getInputStream(); 	    
		    BufferedReader br = new BufferedReader(new InputStreamReader(inputstream));
	    	
			while ((line = br.readLine()) != null) {
				sourcexml = sourcexml + line;
			}
				
			JSONObject jsonObject = XML.toJSONObject(sourcexml,true);
			
			
			value = findValueByKey(jsonObject,"Token");
			
			token = value.toString();
			
			trace.addInfo("token:"+ token);
			
			conf.put(TOKEN, token);
			
			value = findValueByKey(jsonObject,"EncryptionPrivateKey");
			
			encryption_private_key = value.toString();
			
			trace.addInfo("key:" + encryption_private_key);
			
			value = findValueByKey(jsonObject,"addSquare");
			
			add_square = value.toString();
			
			value = findValueByKey(jsonObject,"bodyEncryptionKey");
			
			body_encryption_key = value.toString();
	
			value = findValueByKey(jsonObject,"data"); //我们要在dt时,按此命名规则约定
					
			String jsonPrettyPrintString = value.toString();
			//String jsonPrettyPrintString = jsonObject.toString(PRETTY_PRINT_INDENT_FACTOR);
			requestData = jsonPrettyPrintString;
		    
			if ( "X".equals(add_square) ) {
				requestData = "[" + requestData + "]";	
			}
			
			//打印json
			trace.addInfo(requestData);
			
			//开始签名
	        byte[] requestDataBytes = requestData.getBytes(StandardCharsets.UTF_8);
	        byte[] timestampBytes = ("&timestamp=" + timestamp).getBytes(StandardCharsets.UTF_8);
	        byte[] newBytes = new byte[requestDataBytes.length + timestampBytes.length];
	        System.arraycopy(requestDataBytes, 0, newBytes, 0, requestDataBytes.length);
	        System.arraycopy(timestampBytes, 0, newBytes, requestDataBytes.length, timestampBytes.length);

	        //生成签名
	        byte[] signature = sign(encryption_private_key, newBytes);
	        String sign = Base64.encodeBase64String(encodeDERSignature(signature));
			
					    
			//写入签名
	        DynamicConfigurationKey SIGN = DynamicConfigurationKey.create("http:/"+"/sap.com/xi/XI/System/REST","X-MBCLOUD-API-SIGN");
		    conf.put(SIGN, sign);			
			
			OutputStream outputstream = transformationOutput.getOutputPayload().getOutputStream();
			
			//请求体
			byte[] encryptedData = encrypt(body_encryption_key,requestDataBytes);
			outputstream.write(encryptedData);
	
			trace.addInfo(requestData);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	    
	
	}

	
	public static Object findValueByKey(JSONObject json, String key) {
	    // 如果当前对象本身就有这个 key
	    if (json.has(key)) {
	        return json.get(key);
	    }

	    // 否则遍历所有 key，递归查找
	    for (String currentKey : json.keySet()) {
	        Object value = json.get(currentKey);

	        if (value instanceof JSONObject) {
	            Object result = findValueByKey((JSONObject) value, key);
	            if (result != null) return result;
	        } else if (value instanceof JSONArray) {
	            JSONArray arr = (JSONArray) value;
	            for (int i = 0; i < arr.length(); i++) {
	                Object item = arr.get(i);
	                if (item instanceof JSONObject) {
	                    Object result = findValueByKey((JSONObject) item, key);
	                    if (result != null) return result;
	                }
	            }
	        }
	    }

	    // 没找到
	    return null;
	}	
	

    /**
     * SM2加密算法
     *
     * @param publicKey 公钥
     * @param data      明文数据
     * @return
     */
	
    public static byte[] encrypt(String publicKey, byte[] data) {
    	
        ECPublicKeyParameters ecPublicKeyParameters = encodePublicKey(Hex.decode(publicKey));
        SM2Engine engine = new SM2Engine();
        engine.init(true, new ParametersWithRandom(ecPublicKeyParameters, new SecureRandom()));

        byte[] bytes = null;
        try {
            byte[] cipherText = engine.processBlock(data, 0, data.length);
            bytes = C1C2C3ToC1C3C2(cipherText);
        } catch (Exception e) {
            //log.warn("SM2加密时出现异常:" + e.getMessage());
        }
        return bytes;
    }

    /**
     * SM2解密算法
     *
     * @param privateKey 私钥
     * @param cipherData 密文数据
     * @return
     */
    public static byte[] decrypt(String privateKey, byte[] cipherData) {
        ECPrivateKeyParameters ecPrivateKeyParameters = encodePrivateKey(Hex.decode(privateKey));
        SM2Engine engine = new SM2Engine();
        engine.init(false, ecPrivateKeyParameters);

        byte[] bytes = null;
        try {
            cipherData = C1C3C2ToC1C2C3(cipherData);
            bytes = engine.processBlock(cipherData, 0, cipherData.length);
        } catch (Exception e) {
            //log.warn("SM2解密时出现异常:" + e.getMessage());
        }
        return bytes;
    }

    /**
     * 签名算法
     *
     * @param privateKey 私钥
     * @param data       明文数据
     * @return
     */
    public static byte[] sign(String privateKey, byte[] data) {
        ECPrivateKeyParameters ecPrivateKeyParameters = encodePrivateKey(hexToByte(privateKey));
        SM2Signer signer = new SM2Signer();
        ParametersWithID parameters = new ParametersWithID(ecPrivateKeyParameters, "1234567812345678".getBytes());
        signer.init(true, parameters);
        signer.update(data, 0, data.length);

        byte[] signature = null;
        try {
            signature = decodeDERSignature(signer.generateSignature());
        } catch (Exception e) {
           // log.warn("SM2签名时出现异常:" + e.getMessage());
        }
        return signature;
    }

    private static byte[] hexToByte(String hex)
            throws IllegalArgumentException {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException();
        }
        char[] arr = hex.toCharArray();
        byte[] b = new byte[hex.length() / 2];
        for (int i = 0, j = 0, l = hex.length(); i < l; i++, j++) {
            String swap = "" + arr[i++] + arr[i];
            int byteInt = Integer.parseInt(swap, 16) & 0xFF;
            b[j] = BigInteger.valueOf(byteInt).byteValue();
        }
        return b;
    }

    private static byte[] C1C2C3ToC1C3C2(byte[] cipherText) throws Exception {
        if (cipherText != null && cipherText.length >= 97) {
            byte[] bytes = new byte[cipherText.length];
            System.arraycopy(cipherText, 0, bytes, 0, 65);
            System.arraycopy(cipherText, cipherText.length - 32, bytes, 65, 32);
            System.arraycopy(cipherText, 65, bytes, 97, cipherText.length - 97);
            return bytes;
        } else {
            throw new Exception("SM2 cipher text error, must be more than 96 bytes and in the format C1||C3||C2.");
        }
    }

    private static byte[] C1C3C2ToC1C2C3(byte[] cipherText) throws Exception {
        if (cipherText != null && cipherText.length >= 97) {
            byte[] bytes = new byte[cipherText.length];
            System.arraycopy(cipherText, 0, bytes, 0, 65);
            System.arraycopy(cipherText, 97, bytes, 65, cipherText.length - 97);
            System.arraycopy(cipherText, 65, bytes, cipherText.length - 32, 32);
            return bytes;
        } else {
            throw new Exception("SM2 cipher text error, must be more than 96 bytes and in the format C1||C3||C2.");
        }
    }

    private static ECPublicKeyParameters encodePublicKey(byte[] value) {
        byte[] x = new byte[32];
        byte[] y = new byte[32];
        System.arraycopy(value, 1, x, 0, 32);
        System.arraycopy(value, 33, y, 0, 32);
        BigInteger X = new BigInteger(1, x);
        BigInteger Y = new BigInteger(1, y);
        ECPoint Q = getSM2Curve().createPoint(X, Y);
        return new ECPublicKeyParameters(Q, getECDomainParameters());
    }

    private static ECCurve getSM2Curve() {
        ECParameterSpec spec = ECNamedCurveTable.getParameterSpec(STD_NAME);
        return spec.getCurve();
    }

    private static ECPrivateKeyParameters encodePrivateKey(byte[] value) {
        BigInteger d = new BigInteger(1, value);
        return new ECPrivateKeyParameters(d, getECDomainParameters());
    }

    private static ECDomainParameters getECDomainParameters() {
        ECParameterSpec spec = ECNamedCurveTable.getParameterSpec(STD_NAME);
        return new ECDomainParameters(spec.getCurve(), spec.getG(), spec.getN(), spec.getH(), spec.getSeed());
    }

    private static byte[] decodeDERSignature(byte[] signature) {
		@SuppressWarnings("resource")
		ASN1InputStream stream = new ASN1InputStream(new ByteArrayInputStream(signature));

        byte[] bytes = new byte[64];
        try {
            ASN1Sequence primitive = (ASN1Sequence) stream.readObject();
            @SuppressWarnings("rawtypes")
			Enumeration enumeration = primitive.getObjects();
            BigInteger R = ((ASN1Integer) enumeration.nextElement()).getValue();
            BigInteger S = ((ASN1Integer) enumeration.nextElement()).getValue();
            byte[] r = format(R.toByteArray());
            byte[] s = format(S.toByteArray());
            System.arraycopy(r, 0, bytes, 0, 32);
            System.arraycopy(s, 0, bytes, 32, 32);
        } catch (Exception e) {
           // log.warn("decodeDERSignature时出现异常:" + e.getMessage());
        }
        return bytes;
    }

    public static byte[] encodeDERSignature(byte[] signature) {
        byte[] r = new byte[32];
        byte[] s = new byte[32];
        System.arraycopy(signature, 0, r, 0, 32);
        System.arraycopy(signature, 32, s, 0, 32);
        ASN1EncodableVector vector = new ASN1EncodableVector();
        vector.add(new ASN1Integer(new BigInteger(1, r)));
        vector.add(new ASN1Integer(new BigInteger(1, s)));

        byte[] encoded = null;
        try {
            encoded = (new DERSequence(vector)).getEncoded();
        } catch (Exception e) {
          //  log.warn("encodeDERSignature时出现异常:" + e.getMessage());
        }
        return encoded;
    }

    private static byte[] format(byte[] value) {
        if (value.length == 32) {
            return value;
        } else {
            byte[] bytes = new byte[32];
            if (value.length > 32) {
                System.arraycopy(value, value.length - 32, bytes, 0, 32);
            } else {
                System.arraycopy(value, 0, bytes, 32 - value.length, value.length);
            }
            return bytes;
        }
    }

	
}
