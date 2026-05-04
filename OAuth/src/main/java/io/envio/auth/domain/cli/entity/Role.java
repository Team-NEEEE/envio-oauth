package io.envio.auth.domain.cli.entity;

public enum Role{
    OWNER, //조직 소유자
    ADMIN, //관리자
    MAINTAINER, // 팀 관리자
    MEMBER, // 일반 팀원
    GUEST // 외부 협력자
}
