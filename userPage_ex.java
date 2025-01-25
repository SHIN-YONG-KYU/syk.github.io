//구매신청 프로세스
	@Override
	public ModelAndView buyProd(ModelAndView mv, HttpServletRequest req) throws Exception {
		HashMap<String, Object> params = ParamGetter.pGetter(req);
		log.debug("params :"+ params);
		
		
		//추가 옵션리스트 json배열 객체로 변환
		String jsonData = params.get("O_LIST").toString();
		log.debug("jsonData :: "+jsonData);
		
		JSONArray jsonArr = new JSONArray();
		jsonArr  = JSONArray.fromObject(jsonData);
		//-------------------------------------------------
		
		boolean buyStatus = true; // 구매신청 상태
		String msg = ""; //return 할 메시지
		
		
		//옵션재고별 더블체크
		for(int i=0; i<jsonArr.size();i++) {
			
			JSONObject jsonObj = jsonArr.getJSONObject(i);
			params.put("OPTION_UUID", jsonObj.get("option_uuid"));
			params.put("OPTION_CNT", jsonObj.get("option_cnt"));
			params.put("OPTION_PRICE", jsonObj.get("option_price"));
			params.put("OPTION_NAME", jsonObj.get("option_name"));
			
			
			HashMap<String, Object> list = prodDao.selectOneOption(params);
			int cnt = jsonObj.getInt("option_cnt"); //추가상품 구매수량
			
			//재고상품 발송상품만 + 당일발송 수량체크
			if(list.get("sell_type").equals("n") || list.get("sell_type").equals("d")) {
				int list_cnt = Integer.parseInt(list.get("option_stock").toString()); // 옵션별 재고수량 재고상품발송만 옵션재고 체크
				if(cnt > list_cnt) {
					buyStatus = false;
					msg = "["+list.get("option_name").toString() +"] 의 재고가 부족합니다. 다시 주문해주세요.";
				}
			}
		}//end for
		
		
		//buyStatus 의 상태 판매자가 정지상태인지 체크로직 추가
		HashMap<String,Object> use_info = prodDao.selectSellerUseStatus(params);
		String use_status = use_info.get("use_status").toString(); 
		if(use_status.equals("n")) {
			msg = "판매자의 사정에 의해 구매가 불가한 상품입니다.";
			buyStatus = false;
		}
		
		
		//수량체크 통과할때만 진행
		if(buyStatus) {
			log.debug("수량체크 통과");
			
			String returnData = "";
			String dateNow = "";
			
			 // [타임 스탬프 객체 생성]
	        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
	        returnData = String.valueOf(timestamp.getTime()); // 타임 스탬프 밀리 세컨드
			
	        //주문번호 타임스탬프(0,7) + uuid + 타임스탬프 뒤에꺼(숫자없이)
	        String order_no = returnData.substring(0,7) + params.get("ADMIN_UUID")+returnData.substring(7); //주문번호
	        params.put("ORDER_NO",order_no);
	        mv.addObject("order_no",order_no);
			
	        
	        String sell_type = params.get("SELL_TYPE").toString();
	        log.debug("sell_type:"+sell_type);
	        
			
			//========================pg 결제관련 날짜값 ======================
			
			//pg 일자와 시간 값 가져오기
			LocalDateTime now = LocalDateTime.now();
	        String day = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
	        String clock = now.format(DateTimeFormatter.ofPattern("HHmmss"));
			
	        log.debug("주문일자 : "+day);
	        log.debug("주문일시 : "+clock);
	        
	        String host = req.getHeader("host");
	        String path = "";
	        if(host.contains("##################")) {
				        path = "https://##################"; //개발서버
      			}else if(host.contains("##################")) {
      			  	path = "https://##################"; //운영서버
      			}else if(host.contains("##################")) {
      			  	path = "https://##################"; //개발서버
				
			      }
	        
	        
	        //총결제가격
			    String total_payment = params.get("TOTAL_PAYMENT").toString();
	        
	        //면세인지 과세인지 판단후 과세상품일시 부가세와 면세상품가액 판별
	        int nfreeAmt = 0;// 과세상품금액
	        int freeAmt = 0 ; //면세상품금액
	        int taxAmt = 0 ; //과세상품에대한 부가세금액
	        String taxFree = params.get("TAXFREE").toString(); // n:과세 y:면세
	        
	        if(taxFree.equals("n")) {
	        	nfreeAmt = Integer.parseInt(total_payment);
	        	int supAmt =  (int)((int) Integer.parseInt(total_payment) / 1.1);
	        	taxAmt = Integer.parseInt(total_payment) - supAmt; //과세상품 부가세금액
	        }else if(taxFree.equals("y")) {
	        	freeAmt = Integer.parseInt(total_payment);
	        }
	        
			
			
			
			//결제정보넘길 해쉬맵
			HashMap<String, Object> param = new HashMap<String, Object>();
			param.put("mid", mid); // 상점 ID
			param.put("payGroup", "OPG");//결제그룹
			param.put("payBrand", "OCC"); // 결제브랜드
			param.put("buyItemnm",params.get("PROD_NAME"));// 상점구매 상품명
			param.put("buyReqamt", total_payment); //상품구매금액
			param.put("orderno", order_no); //주문번호
			param.put("orderdt", day); //주문일자
			param.put("ordertm", clock); //주문시간
			param.put("rUrl",path + "##################"); //리턴 url
			
			param.put("nfreeAmt",nfreeAmt); //과세상품금액
			param.put("freeAmt",freeAmt); //면세상품금액
			param.put("taxAmt",taxAmt); //과세상품부가세
			
			//주문번호 + 주문일자 + 주문시간 + 구매금액
			String Message = order_no + day + clock + total_payment ;
			//암호화후 나온 토큰값
			String checkHash= HmacSha256Enc.getHmac(Message, api_key);
			
			param.put("checkHash", checkHash);
			
			
			//params map데이터 json String 데어터로 가공 Jackson라이브러리
			ObjectMapper mapper = new ObjectMapper();
			String json_text = mapper.writeValueAsString(params);
			log.debug("json_text "+json_text);
			
			//담김상품 파라미터 정보 aes암호화처리와 url인코드까지 같이
			String hash_text = AES.encrypt(json_text);
			String url_text	= URLEncoder.encode(hash_text,"UTF-8");
			
			log.debug("hash_text :: "+hash_text);
			log.debug("url_text :: "+url_text);
			
			param.put("reserved01",url_text); //상품판매정보가담긴 정보내용
			
			//====================가상계좌일때 ===============
			if(params.get("P_METHOD").toString().equals("vacnt")) {
				
				param.put("payType", "VA"); //결제수단 신용카드 : CC , 계좌이체 :AT ,가상계좌 : VA
				
				
			}else if(params.get("P_METHOD").toString().equals("card")) {
				
				param.put("payType", "CC"); //결제수단 신용카드 : CC , 계좌이체 :AT ,가상계좌 : VA
				
			}
			
			mv.addObject("pgData", param);
			
			
			return Return.jsonView_msg_result(mv, msg, true);
		}else {
			
			
			return Return.jsonView_msg_result(mv, msg, false);
		}
		
	}
