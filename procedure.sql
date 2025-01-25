CREATE DEFINER=`############` PROCEDURE `banner_date_prove`(in _s_date varchar(20) , in _diff_day int, in _banner_type varchar(20) )
BEGIN
	
	declare _day varchar(20);  -- 요일
	DECLARE X INT ;	 	 -- 초기시작일 증가
	DECLARE _cnt INT;	 -- 날짜에 배너등록한 사람의 수
	declare _status int; -- 루푸문에서 break가 걸렸는지 안걸렸는지 판별
	
	
	/* 에러 처리위한 상태값*/
	DECLARE err_ INT DEFAULT 0;
	DECLARE CONTINUE HANDLER FOR SQLEXCEPTION SET err_ = 1;
	
	START TRANSACTION;
	BLOCK: BEGIN	
	
	
	set _status = 0; -- status 초기설정 0이면 이상없음 1이면 제한
	set x = 0 ; -- x의초기 추가일수 1설정
	
	--  시작이로부터 추가일수 1부터 총일수까지 루프
	loop_label:LOOP
		
		
		IF X > _diff_day THEN
			LEAVE loop_label;
		END IF;
		
		
		-- 추가일수로 요일구하기
		select date_add(_s_date,interval x day) into _day ;
		
		
		SELECT COUNT(*) INTO _cnt
		FROM `banner_info`
		WHERE DATE_FORMAT(_day,'%Y%m%d') >= DATE_FORMAT(s_date,'%Y%m%d') AND DATE_FORMAT( _day ,'%Y%m%d') <= DATE_FORMAT(e_date,'%Y%m%d')
		AND banner_type = _banner_type
		AND banner_status NOT IN ('den');
		
		
		IF 0 != err_ THEN
			SELECT 'e' result,'에러 : loop select절 오류' msg;
			LEAVE BLOCK;
			ROLLBACK;
		END IF;
		
		
		-- 배너등록한 사람의 수가 5명보다 많으면 break 걸고 안된다고 뛰워준다.
		if _cnt > 5 then
			set _status = 1;
		
			SELECT 'a' result,concat(' ( ',_day,' ) 에러 : 등록이 불가능한 날짜입니다') msg;
			LEAVE BLOCK;
		end if;
		
		SET X = X+1;
		
		
	END LOOP;
		
		-- result c 일떄만 정상동작 , a 날짜등록불가에러 , e select에러
		-- 루프가 끊나고 판별
		if _status = 0 then
		
			select 'c' result,'날짜정보에 이상이 없습니다.' msg;
		end if;
	
	END BLOCK;
    END */$$
DELIMITER ;
