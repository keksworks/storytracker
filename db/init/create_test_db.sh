#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-SQL
    create database ${POSTGRES_USER}_test;
    create database ${POSTGRES_USER}_e2e;
SQL
