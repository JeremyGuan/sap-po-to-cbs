package com.sap.java.modules;

import com.sap.java.modules.SM2Util;
import javax.ejb.Stateless;
import com.sap.aii.af.lib.mp.module.Module;
import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;
import com.sap.aii.af.lib.mp.module.ModuleException;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.api.XMLPayload;
import com.sap.engine.interfaces.messaging.api.exception.InvalidParamException;
import com.sap.engine.interfaces.messaging.api.exception.MessagingException;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditLogStatus;
import java.io.IOException;
import java.io.InputStream;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditAccess;
import org.apache.commons.io.IOUtils;
import com.sap.engine.interfaces.messaging.api.PublicAPIAccessFactory;
import org.json.*;

/**
 * Session Bean implementation class cbs8Decryption
 */
@Stateless
public class tooltest implements Module {

	/**
	 * Default constructor.
	 */
	public ModuleData process(ModuleContext mc, ModuleData imd) throws ModuleException {
		// TODO Auto-generated constructor stub

		AuditAccess msgAuditAccessor;
		try {
			msgAuditAccessor = PublicAPIAccessFactory.getPublicAPIAccess().getAuditAccess();
			String jsonStr;
			Object obj = imd.getPrincipalData();
			Message msg = (Message) obj;
			MessageKey amk = new MessageKey(msg.getMessageId(), msg.getMessageDirection());
			XMLPayload xp = msg.getDocument();
			// 原始数据

			InputStream result = xp.getInputStream();
						
			//msgAuditAccessor.addAuditLogEntry(amk, AuditLogStatus.SUCCESS, result.toString());

			// 私钥s
			String key = mc.getContextData("bodyDecryptionKey");
			String nodeName = mc.getContextData("nodeName");
			String nameSpace = mc.getContextData("nameSpace");

			//msgAuditAccessor.addAuditLogEntry(amk, AuditLogStatus.SUCCESS, key);

			// 解密

			byte[] responseData = IOUtils.toByteArray(result);

			//msgAuditAccessor.addAuditLogEntry(amk, AuditLogStatus.SUCCESS, responseData.toString());

			byte[] res = SM2Util.decrypt(key, responseData);

			//msgAuditAccessor.addAuditLogEntry(amk, AuditLogStatus.SUCCESS, res.toString());

			jsonStr = new String(res, "UTF-8");
			
			JSONObject jsonobj = new JSONObject(jsonStr);
			
			String xmlStr = XML.toString(jsonobj);
			
			xmlStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
   		             "<ns0:" + nodeName + " xmlns:ns0=\"" + nameSpace + "\">" +
   		             xmlStr +
   		             "</ns0:" + nodeName + ">";
	
			//msgAuditAccessor.addAuditLogEntry(amk, AuditLogStatus.SUCCESS, jsonStr);
			xp.setContent(xmlStr.getBytes());
			xp.setContentType("application/xml");

			imd.setPrincipalData(msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidParamException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (MessagingException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		return imd;
	}

}
