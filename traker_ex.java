
/**
 * 
 * sweettracker 배송추적 api 사용 유틸
 */

public class SweetTracker {

	public static String tier  ="#####"; 
	public static String key ="#####"; 
	
	public static String callback_url = "#####";//전달받을 url 운영서버
	public static String callback_type = "map";//전달받을 타입
	public static String type = "json";//response type
	
	/서버 api
	public static String API_DOMAIN = "#####";
	
	
	/**
	 * 운송장 등록 요청(송장번호,배송코드,fid(식별값)
	 * 
	 * req = success,num,fid,e_message,e_code
	 * 
	 * @throws IOException 
	 */
	public static HashMap<String, Object> add_invoice(String d_num,String d_code,String fid) throws IOException {
		System.out.println("송장등록실행");
		
		String url = API_DOMAIN+"/add_invoice";
		
		JSONObject obj = new JSONObject();
		obj.put("key",key);
		obj.put("tier",tier);
		obj.put("num",d_num);
		obj.put("code",d_code); //배송사코드
		obj.put("fid", fid); //거래고유번호
		obj.put("callback_url",callback_url); 
		obj.put("callback_type",callback_type);
		obj.put("type",type);
		
		return post(url,obj);
		
		//return post_add_invoice(d_num,d_code,fid);
	}
	
	
	
	/**
	 * 운송장 유효성검사 (송장번호,배송코드)
	 * 
	 * req = code,suceess(true,false),num
	 * 
	 * @throws IOException 
	 */
	public static HashMap<String, Object> validate(String d_num,String d_code) throws IOException {
		System.out.println("유효성 검사 실행");
		
		String url = API_DOMAIN+"/validate";
		
		JSONObject obj = new JSONObject();
		obj.put("num",d_num);
		obj.put("code",d_code); //배송사코드
		
		return post(url,obj);
		
		//return post_add_invoice(d_num,d_code,fid);
	}
	
	
	

	
	
	private static JSONObject post(String url, JSONObject params) {
		String sb = null;
		HttpURLConnection con = null;
		BufferedReader br = null;

		System.out.println("API Url : " + url);

		try {
			URL object = new URL(url);

			con = (HttpURLConnection) object.openConnection();

			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json; utf-8");
			con.setRequestProperty("Accept", "application/json");

			if (params != null) {
				try (OutputStream os = con.getOutputStream()) {
					byte request_data[] = params.toJSONString().getBytes("utf-8");
					os.write(request_data);
					os.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			int HttpResult = con.getResponseCode();
			System.out.println("HttpResult : " + HttpResult);

			if (HttpResult == HttpURLConnection.HTTP_OK) {
				br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
				sb = "";
				String line = null;

				while ((line = br.readLine()) != null) {
					sb = sb + line + "\n";
				}

				br.close();
			} else {
				System.out.println(con.getResponseMessage());
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (con != null) { 
				con.disconnect();
				con = null;
			}

			if (br != null) {
				try {
					br.close();
					br = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return stringToJSON(sb);
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

