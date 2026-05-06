// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /app/add */
export async function addApp(body: API.AppAddRequest, options?: { [key: string]: any }) {
  return request<API.ResultLong>('/app/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /app/chat/gen/code */
export async function chatToGenCode(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.chatToGenCodeParams,
  options?: { [key: string]: any }
) {
  return request<API.ServerSentEventString[]>('/app/chat/gen/code', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 DELETE /app/delete */
export async function deleteApp(body: API.DeleteRequest, options?: { [key: string]: any }) {
  return request<API.ResultBoolean>('/app/delete', {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /app/deploy */
export async function deployApp(body: API.AppDeployRequest, options?: { [key: string]: any }) {
  return request<API.ResultString>('/app/deploy', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 PUT /app/edit */
export async function editApp(body: API.AppEditRequest, options?: { [key: string]: any }) {
  return request<API.ResultBoolean>('/app/edit', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /app/featured/page */
export async function listFeaturedApp(body: API.AppQueryRequest, options?: { [key: string]: any }) {
  return request<API.ResultPageAppVO>('/app/featured/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /app/get/${param0} */
export async function getApp(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getAppParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params
  return request<API.ResultAppVO>(`/app/get/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /app/getInfo/${param0} */
export async function getAppInfo(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getAppInfoParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params
  return request<API.ResultAppVO>(`/app/getInfo/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /app/my/page */
export async function listMyApp(body: API.AppQueryRequest, options?: { [key: string]: any }) {
  return request<API.ResultPageAppVO>('/app/my/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /app/page */
export async function selectPageByAdmin(
  body: API.AppQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultPageAppVO>('/app/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 DELETE /app/remove */
export async function removeApp(body: API.DeleteRequest, options?: { [key: string]: any }) {
  return request<API.ResultBoolean>('/app/remove', {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 PUT /app/update */
export async function updateApp(body: API.AppUpdateRequest, options?: { [key: string]: any }) {
  return request<API.ResultBoolean>('/app/update', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
