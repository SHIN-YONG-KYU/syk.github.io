/**
 * 
 * tnc pg cancel 기능 동작
 * 
 */

public class CancelPg {

	//public static String API_DOMAIN = "################";
	public static String API_DOMAIN = " ################";
	public static String CARD_URI = "################";
	
	
	/**
	 * pg 취소로직(결제수단,거래번호,취소금액)
	 * @throws IOException 
	 */
	public static HashMap<String, Object> pgCancel(String pay_method,String tid,String cancel_amt) throws IOException {
		
		//checkhash 생성
		//tid + mid + cancel_amt
		String Message = tid + Security.MID + cancel_amt ;
		//암호화후 나온 토큰값
		String checkHash= HmacSha256Enc.getHmac(Message, Security.API_KEY);
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		map.put("mid", Security.MID);
		map.put("pay_method", pay_method);
		map.put("tid", tid);
		map.put("cancel_amt", cancel_amt);
		//map.put("tax_yn", "Y");
		map.put("checkhash", checkHash);
		map.put("nfree_amt", 0);
		map.put("free_amt", cancel_amt);
		map.put("tax_amt", 0);
		
		
		return post(map);
	}


	
	
	

	private static HashMap<String, Object> post(HashMap<String, Object> params)  throws IOException {
		
		System.out.println("params :: "+ params);
		
		String pay_method = params.get("pay_method").toString();
		String domain = "";
		if(pay_method.equals("CC")) {
			domain = API_DOMAIN + CARD_URI ;
		}
		
		//json으로 변환시킨다.
		ObjectMapper mapper = new ObjectMapper();
		String json_text = mapper.writeValueAsString(params);
		byte[] postDataBytes = json_text.toString().getBytes("UTF-8");
		
		
		URL url = new URL(domain);
		String line;
		StringBuilder sb = new StringBuilder();
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("content-type" ,"application/json ; charset=utf-8");
		conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
		conn.setDoOutput(true);
		conn.getOutputStream().write(postDataBytes);

		// API 응답메시지를 불러와서 문자열로 저장
		BufferedReader rd;
		if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
		    rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		} else {
		    rd = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
		}
		while ((line = rd.readLine()) != null) {
		    sb.append(line);
		}
		rd.close();
		conn.disconnect();
		String text = sb.toString();
		System.out.println(text);
		
		//가져온 json 값들을 map으로 파싱한다.
		HashMap<String, Object> map = mapper.readValue(text, HashMap.class);
		
        
        return map;
	}

}
