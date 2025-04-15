#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-SQL
    create database ${POSTGRES_USER}_test;
    grant all privileges on database ${POSTGRES_USER}_test to $POSTGRES_USER;

    create database ${POSTGRES_USER}_e2e;
    grant all privileges on database ${POSTGRES_USER}_e2e to $POSTGRES_USER;
SQL
