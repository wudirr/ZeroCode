// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 PUT /user/edit */
export async function edit(body: API.UserEditRequest, options?: { [key: string]: any }) {
  return request<API.ResultBoolean>('/user/edit', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /user/get/login */
export async function getLoginUser(options?: { [key: string]: any }) {
  return request<API.ResultUserQueryVO>('/user/get/login', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /user/getInfo/${param0} */
export async function getInfo(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getInfoParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params
  return request<API.ResultUserVO>(`/user/getInfo/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /user/list */
export async function list(options?: { [key: string]: any }) {
  return request<API.ResultListUserVO>('/user/list', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /user/login */
export async function loginUser(body: API.UserLoginRequest, options?: { [key: string]: any }) {
  return request<API.ResultUserLoginVO>('/user/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /user/logout */
export async function userLogout(options?: { [key: string]: any }) {
  return request<API.ResultBoolean>('/user/logout', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /user/page */
export async function page(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.pageParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultPageUserVO>('/user/page', {
    method: 'GET',
    params: {
      ...params,
      userQueryRequest: undefined,
      ...params['userQueryRequest'],
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /user/register */
export async function registerUser(
  body: API.UserRegisterRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultLong>('/user/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 DELETE /user/remove */
export async function remove(body: API.DeleteRequest, options?: { [key: string]: any }) {
  return request<API.ResultBoolean>('/user/remove', {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /user/save */
export async function save(body: API.UserSaveRequest, options?: { [key: string]: any }) {
  return request<API.ResultBoolean>('/user/save', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 PUT /user/update */
export async function update(body: API.UserUpdateRequest, options?: { [key: string]: any }) {
  return request<API.ResultBoolean>('/user/update', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
