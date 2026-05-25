포트 = 5000 
main.py 백그라운드 실행 및 종료법 
가상환경 실행 /bin source activate 
/home/ubuntu/etl_python 에 cd로 이동후 nohup python main.py & 로 백그라운드 실행 
종료하는거는 ps -ef | grep python 번호 찾아서 kill -9 12345 로 종료하던지 pkill -f main.py 이걸로 한번에 종료
종료후 jobs 라고 치면 아무것도 안나옴 나오면 실행중인거임 

포트 = 3000
node server.js 백그라운드 실행 및 종료법 
/home/ubuntu/etl_node/sftp_node/gateway-node 에서 pm2 start server.js --name "gateway-server" 로 실행 "" 은 모니터링 이름설정 
pm2 list 로 동작중인거 확인 
pm2 stop gateway-server 일시정지 
pm2 start gateway-server 실행 
pm2 delete gateway-server pm2 에서 날려버림  명령어 
pm2 start server.js --name "gateway-server" 실행하는 명령어 
# pm2가 모아둔 로그 파일들의 위치와 용량 확인
pm2 logs --lines 1

etl-backend.war 백그라운드 실행 및 종료법 
/var/lib/tomcat10/webapps 에 존재 cd / 해야 /var 들어갈수 있음 

 
포트 = 8080
# 톰캣 서버 구동하기 (자동으로 백그라운드 실행됨) 위치상관없음
sudo systemctl start tomcat10

# 톰캣 서버 종료하기 (자바 백엔드가 꺼짐)
sudo systemctl stop tomcat10

# 톰캣 서버 상태 확인하기 (잘 돌고 있나 초록불 확인)
sudo systemctl status tomcat10

# 톰캣 실시간 통합 로그 확인하기 (나갈 땐 Ctrl + C)
tail -f /var/log/tomcat10/catalina.out

3개 실행후 AWS 공인 아이피 주소:3000 번으로 접속 



로컬에서 AWS 서버로 데이터 전송하는 ETL 은 
스프링 jetty 사용  스프링 포트 = 8081 
C:\Users\SMT15\Project_ETL_insert\spring-backend 에서 mvn exec:java 로 실행 

프론트 서버 는 로컬호스트:4000 으로 접속 
C:\Users\SMT15\Project_ETL_insert\frontend-api 에서 node server.js 로 실행 

파이썬 실행 포트= 5001 
C:\Users\SMT15\Project_ETL_insert\python-etl 에서 python app.py 로 실행 
AWS 공인주소가 매번 달라지니 app.py 에서 DB_CONFIG 에서 호스트 주소 매번 수정후 실행!!








