CREATE DEFINER=`#############` EVENT `1day_schedule` ON SCHEDULE EVERY 1 DAY STARTS '2023-05-15 00:00:05' ON COMPLETION NOT PRESERVE ENABLE DO BEGIN
      -- 배너의 시작,종료 상태 변경
	    call banner_ing_change();
	    -- 예약 단가의 변경
	    call meat_price_change();
	    -- 대량주문 배송완료 변경
	    call meat_big_order_complete();
	    
	    -- 이벤트상태 변경
	    call meat_event_change();
	    
	    -- 매일이벤트 종료기간 체크후 상품 종료
	    call meat_event_prod_stop();
	    
	    -- 대량주문테이블 정산가능 상태값 변경
	    call meat_big_order_delivery_cal_status();
	    
	    -- 발송완료후 일주일후에 레벨값 변경
	    call puchase_delivery_level();
	END 
DELIMITER ;
