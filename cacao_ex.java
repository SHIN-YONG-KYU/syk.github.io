/**
 * 
 * aligo 알림톡 api 유틸
 */

public class CacaoTok {

	public static String APIKEY = "########################";
	public static String USERID = "###############";
	public static String SENDERKEY = "##############################";
	public static String TOKEN = "";
	public static String SENDER = "###################";
	public static String PLUSID = "################";
	public static String PHONENUMBER = "################";
	
	
	//알리고 api도메인
	public static String API_DOMAIN = "https://kakaoapi.aligo.in";
	
	
	
	
	/**
	 * 알리고 알림톡 api호출 토큰 생성
	 * 
	 * 
	 * 
	 * @throws IOException 
	 */
	public static void token() throws IOException {
		System.out.println("알리고 토큰 발행 실행");
		
		String url = API_DOMAIN+"/akv10/token/create/1/i/";
		
		MultiValueMap<String,Object> obj = new LinkedMultiValueMap<>();
		obj.add("apikey",APIKEY);
		obj.add("userid",USERID); 
		
		
		try {
			//토큰발급받아서 저장
			HashMap<String, Object> info = post(url, obj);
			System.out.println("token 발행정보 ::"+info);
			TOKEN = info.get("token").toString(); //토큰 저장
		}catch (Exception e) {
			// TODO: handle exception
			System.out.println("token 발행 실패");
		}
		
	}
	
	/**
	 * 알리고 알림톡 카카오채널 인증
	 * 
	 * 
	 * 
	 * @throws IOException 
	 */
	public static void auth() throws IOException {
		System.out.println("카카오톡 인증  실행");
		
		String url = API_DOMAIN+"/akv10/profile/auth/";
		
		token();
		
		MultiValueMap<String,Object> obj = new LinkedMultiValueMap<>();
		obj.add("apikey",APIKEY);
		obj.add("userid",USERID); 
		obj.add("token",TOKEN); 
		obj.add("plusid",PLUSID); 
		obj.add("phonenumber",PHONENUMBER); 
		
		//카카오채널인증
		HashMap<String, Object> info = post(url,obj);
		System.out.println("인증 요청 정보 ::"+info);
		if(info.get("code").toString().equals("0")) {
			System.out.println("카카오톡 정상호출완료");
		}
		
	}
	
	
	
	/**
	 * 알림톡 전송 
	 * 
	 * 
	 * 
	 * @throws IOException 
	 */
	public static HashMap<String, Object> transmit(String tpl_code,String receiver_1,String subject_1,String message_1,String emtitle) throws IOException {
		
		String url = API_DOMAIN+"/akv10/alimtalk/send/";
		
		token(); //토큰키 발급받음
		
		
		System.out.println("알림톡 전송 실행");
		MultiValueMap<String,Object> obj = new LinkedMultiValueMap<>();
	
		obj.add("apikey",APIKEY);
		obj.add("userid",USERID);
		obj.add("token",TOKEN);
		obj.add("senderkey",SENDERKEY);
		obj.add("sender",SENDER);
		obj.add("tpl_code",tpl_code);
		obj.add("receiver_1",receiver_1);
		obj.add("subject_1",subject_1);
		obj.add("message_1",message_1);
		
		
		//템플릿별로 포함되는 정보처리 강조표기들어가는 템플릿
		if(tpl_code.equals("####") || tpl_code.equals("####") || tpl_code.equals("####") ||tpl_code.equals("####") || tpl_code.equals("####") || tpl_code.equals("####")
				|| tpl_code.equals("####") || tpl_code.equals("####")) {
			//강조표기형 처리
			obj.add("emtitle_1",emtitle);
		}
		
		//버튼이 들어가는 템플릿
		if(tpl_code.equals("####") ||tpl_code.equals("####") || tpl_code.equals("####") || tpl_code.equals("####")) {
			
			//버튼 json 파일 만들기 형식에맞춰서
			JSONObject button = new JSONObject();
			button.put("name", "####");
			button.put("linkType", "WL");
			button.put("linkTypeName", "웹링크");
			button.put("linkM", "####"); //모바일링크
			button.put("linkP", "####"); //pc링크
			
			JSONArray button_1 = new JSONArray();
			button_1.add(button);
			
			JSONObject data = new JSONObject();
			data.put("button", button_1);

      //형식에 맞추기 위해서
			obj.add("button_1",data.toString()); //json string 으로 변환해서 집어넣기
		}
		
		
		
		return post(url,obj);
		
	}
	
	

	
	
	private static JSONObject post(String url, MultiValueMap<String, Object> params) throws UnsupportedEncodingException {
		
		
		
		HttpHeaders headers = new HttpHeaders();
		
		//headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		
		
		System.out.println("바디입력값 :: "+params);
		HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(params,headers);
		
		RestTemplate rt = new RestTemplate();
		rt.getMessageConverters().add(0,new StringHttpMessageConverter(StandardCharsets.UTF_8));
		ResponseEntity<String> response = rt.postForEntity(URI.create(url), entity,String.class);
		
		//System.out.println("리턴바디값 :: "+stringToJSON(response.getBody()));
		//System.out.println("리턴헤드값 :: "+response.getHeaders());
		//System.out.println("리턴상태코드 :: "+response.getStatusCode());
		
		return stringToJSON(response.getBody());
		
	}
	
	

	private static JSONObject stringToJSON(Object strJson) {
		if (strJson == null)
			return null;

		JSONParser jsonParser = new JSONParser();

		Object obj = null;
		try {
			obj = jsonParser.parse(strJson.toString());
		} catch (ParseException e) {
		}

		return (JSONObject) obj;
	}
}
