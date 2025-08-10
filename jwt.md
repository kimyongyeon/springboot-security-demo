# 1) 로그인 → 토큰 발급
curl -s -X POST http://localhost:8080/auth/login \
-H "Content-Type: application/json" \
-d '{"username":"user","password":"password"}' | jq .

# 로그인 → JWT 발급
curl -s -X POST http://localhost:8080/auth/login \
-H "Content-Type: application/json" \
-d '{"username":"admin","password":"password"}'

# 2) 토큰으로 보호 API 접근
ACCESS_TOKEN=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiaXNzIjoiZGVtby1hdXRoIiwiaWF0IjoxNzU0Nzk3MDkzLCJleHAiOjE3NTQ4MDA2OTMsInJvbGVzIjpbIlJPTEVfVVNFUiJdfQ.3_jX92Hm9Of5NnQ3aRdLzXIs8p0eWWFmrkvFYrsUAk0

curl -H "Authorization: Bearer $ACCESS_TOKEN" http://localhost:8080/api/user/ping

ACCESS_TOKEN=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlzcyI6ImRlbW8tYXV0aCIsImlhdCI6MTc1NDc5NzIyMSwiZXhwIjoxNzU0ODAwODIxLCJyb2xlcyI6WyJST0xFX0FETUlOIl19.pT98qDz09FdAXAjfgQ3AhacDkcSLzAPZeOFwilOXoKw
curl -H "Authorization: Bearer $ACCESS_TOKEN" http://localhost:8080/api/admin/ping   # admin 토큰 필요
