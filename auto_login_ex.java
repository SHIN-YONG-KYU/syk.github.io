=============================== 로그인 시점에 자동로그인 체크할경우 =======================================
//자동로그인상태 체크
  String auto_check = params.get("AUTO_ID_STATUS").toString();
  if(auto_check.equals("true")) {
    
    //자동로그인 체크할 파라미터값
    HashMap<String, Object> auto_info = new HashMap<String, Object>();
    auto_info.put("IP", ip);
    auto_info.put("ADMIN_UUID", admin_uuid);
    auto_info.put("DEVICE", deList[0]);
    auto_info.put("STATE", strList[0]);
    
    //기존에 자동로그인 되있는지 확인
    HashMap<String, Object> auto_cnt = adminDao.selectCntAutoLogin(auto_info);
    String cnt_admin = auto_cnt.get("cnt_admin").toString(); //uuid로가져온 전체 수 (1아니면 0)
    String cnt_ip = auto_cnt.get("cnt_ip").toString(); // 현재ip 와 등록된 ip 비교 (1아니면 0)
    String cnt_device = auto_cnt.get("cnt_ip").toString(); // 현재device 와 등록된 device 비교 (1아니면 0)
    
    //등록된 ip와 전체수가 같지않을떄 => 다른곳에 등록된 ip 이다 
    if(!cnt_admin.equals(cnt_ip)) {
      //기존등록된 db데이터 삭제 
      adminDao.deleteAutoLogin(auto_info);
      //새로운 자동로그인상태 등록
      adminDao.insertAutoLogin(auto_info);
    }else {
      
      //ip와 admin 의 수가 같으면 => 등록이 안되어있거나 or ip는 같지만 다른장치
      //전체DB등록수 0이면 등록
      if(cnt_admin.equals("0")) {
        //자동로그인상태등록
        adminDao.insertAutoLogin(auto_info);
      }else {
        //cnt_amdin 1이고 cnt_ip 도 1이다 ip는같지만 다른장치인지판별
        if(cnt_device.equals("0")) {
          //기존등록된 db데이터삭제후 
          adminDao.deleteAutoLogin(auto_info);
          //자동로그인상태등록
          adminDao.insertAutoLogin(auto_info);
        }
      }
    }
  }//자동로그인 체크 상태
  //자동로그인 로직 끝
========================================================================

================ 거래처 접속했을떄 기본페이지 에서 자동로그인 체크 ==================

//클라이언트 접속정보
		HashMap<String, Object> info = LoginManager.getClientIp(req);
		String ip = info.get("ip").toString();
		String state = info.get("state").toString();
		String[] strList = state.split(",");
		String device = info.get("device").toString();
		device = Arrays.toString(findBracketTextByPattern(device).toArray());
		String[] deList = device.split(",");
		deList[0] = deList[0].replaceAll("\\[", "").replaceAll("\\]","");
		
		//자동로그인 체크할 파라미터값
		HashMap<String, Object> auto_info = new HashMap<String, Object>();
		auto_info.put("IP", ip);
		auto_info.put("DEVICE", deList[0]);
		auto_info.put("STATE", strList[0]);
		
		HashMap<String, Object> check = adminDao.selectAutoLoginCheck(auto_info);
		int cnt = Integer.parseInt(check.get("cnt").toString()); //ip와 장치가 일치하는지여부
		
		//자동로그인 일치값이있는지체크
		if(cnt > 0) {
			
			String admin_uuid = check.get("admin_uuid").toString(); //일치하는 admin_uuid
			//자동로그인동작
			HttpSession session = req.getSession();
			params.put("ADMIN_UUID", admin_uuid);
			
			HashMap<String, Object> adminInfo= adminDao.selectAdminInfo(params);
			// 세션에 회원 정보 저장
			session.setAttribute("isLogin", true);
			adminInfo.put("ip", ip);
			session.setAttribute("ip", ip);
			session.setAttribute("adminInfo", adminInfo);
			log.debug("정상 로그인 입니다. "+adminInfo);
			
			String admin_id = adminInfo.get("ADMIN_ID").toString();
			LoginManager.getInstance().setSession(session, admin_id);
			
			mv.setViewName("redirect:/action/prod/sale");
			
		}else {
			//로그인페이지로 이동
			log.debug("KEY============"+  ReCaptcha.getSiteKey(req));
			mv.addObject("s_msg",params.get("S_MSG"));
			mv.addObject("site_key", ReCaptcha.getSiteKey(req));
			mv.setViewName(view_folder+"loginForm");
		}
