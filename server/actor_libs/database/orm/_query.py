from flask import g
from sqlalchemy import inspection

from actor_libs.types.orm import BaseQueryT
from actor_libs.errors import DataNotFound
from .utils import (
    base_filter_tenant, filter_api, filter_group, filter_request_args,
    sort_query, dumps_query_result, paginate, get_model_schema
)


class ExtendQuery(BaseQueryT):

    def _get_query_model(self):
        """ Get database model """

        entity = self._mapper_zero()
        insp = inspection.inspect(entity)
        if insp.is_selectable:
            entity = insp.c
        elif insp.is_aliased_class:
            entity = insp.entity
        elif hasattr(insp, "mapper"):
            entity = insp.mapper.class_
        else:
            entity = entity
        return entity

    def filter_tenant(self, tenant_uid=None):
        """ Filter tenant"""

        model = self._get_query_model()
        query = base_filter_tenant(model, self, tenant_uid)
        if g.get('app_uid'):
            query = filter_api(model, query)  # filter api
        else:
            query = filter_group(model, query)  # filter group
        return query

    def first_or_404(self):
        query = self.filter_tenant()
        result = query.first()
        if result is None:
            raise DataNotFound()
        return result

    def to_dict(self, **kwargs):
        """ Query result to dict with schema """

        query_result = self.first_or_404()
        record = dumps_query_result(query_result, **kwargs)
        return record

    def pagination(self, code_list=None, is_filter_tenant=True):
        model = self._get_query_model()
        if is_filter_tenant:
            query = self.filter_tenant()  # filter tenant
        else:
            query = self
        query = sort_query(model=model, query=query)  # sort query
        query = filter_request_args(model=model, query=query)  # filter request args
        return paginate(query, code_list)

    def select_options(self, attrs: list = None, is_limited=True, is_filter_tenant=True):
        """
        Return select_options record
        :param attrs: attr list
        :param is_limited: Whether to limit the database results,limit 10 records if False
        :param is_filter_tenant: Whether to filter the tenant
        :return: records
        """
        model = self._get_query_model()
        if is_filter_tenant:
            query = self.filter_tenant()
        else:
            query = self
        query = filter_request_args(model, query)
        query = sort_query(model, query)

        if is_limited:
            results = query.limit(10).all()
        else:
            results = query.all()
        records = []
        for result in results:
            record = {
                'label': result.label,
                'value': result.value
            }
            if attrs:
                record_attr = {}
                for attr in attrs:
                    record_attr[attr] = getattr(result, attr)
                record['attr'] = record_attr
            records.append(record)

        return records

    def many(self, limit: int = None, allow_none=True, expect_result: int = None):
        query = self.filter_tenant()
        if limit:
            query = query.limit(limit)
        result = query.all()

        if not allow_none and not result:
            raise DataNotFound(field='url')
        if not allow_none and len(result) != expect_result:
            raise DataNotFound(field='url')
        return result
