declare namespace API {
  type getInfoParams = {
    id: number
  }

  type pageParams = {
    userQueryRequest: UserQueryRequest
  }

  type PageUserVO = {
    records?: UserVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type removeParams = {
    id: number
  }

  type ResultBoolean = {
    code?: number
    data?: boolean
    message?: string
  }

  type ResultListUserVO = {
    code?: number
    data?: UserVO[]
    message?: string
  }

  type ResultLong = {
    code?: number
    data?: number
    message?: string
  }

  type ResultPageUserVO = {
    code?: number
    data?: PageUserVO
    message?: string
  }

  type ResultUserLoginVO = {
    code?: number
    data?: UserLoginVO
    message?: string
  }

  type ResultUserQueryVO = {
    code?: number
    data?: UserQueryVO
    message?: string
  }

  type ResultUserVO = {
    code?: number
    data?: UserVO
    message?: string
  }

  type UserEditRequest = {
    id?: number
    userPassword?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserLoginRequest = {
    userAccount?: string
    userPassword?: string
  }

  type UserLoginVO = {
    id?: number
    userAccount?: string
    userName?: string
  }

  type UserQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    userAccount?: string
    userName?: string
    userRole?: string
    createTime?: string
  }

  type UserQueryVO = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
  }

  type UserRegisterRequest = {
    userAccount?: string
    userPassword?: string
    checkPassword?: string
  }

  type UserSaveRequest = {
    userAccount?: string
    userPassword?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserUpdateRequest = {
    id?: number
    userPassword?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserVO = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    editTime?: string
    createTime?: string
    updateTime?: string
  }
}
