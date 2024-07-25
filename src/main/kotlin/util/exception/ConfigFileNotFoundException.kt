package util.exception

class ConfigFileNotFoundException : RuntimeException("루트 경로의 .config 파일을 찾을 수 없습니다.")