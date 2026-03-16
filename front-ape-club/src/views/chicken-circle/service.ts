import req from '@utils/request'

export const RequestUrl = {
  CircleList: '/circle/share/circle/list',
  MomentSave: '/circle/share/moment/save',
  GetMoments: '/circle/share/moment/getMoments',
  CommentSave: '/circle/share/comment/save',
  CommentList: '/circle/share/comment/list'
}

const baseService = ({ method = 'get', url = '', params = {} }) => {
  const reqParam = {
    method,
    url
  }
  if (method === 'get') {
    reqParam.params = params
  }
  if (method === 'post') {
    reqParam.data = params
  }
  return req(reqParam, '/circle')
}

export const fetchCircleList = () => {
  return baseService({
    url: RequestUrl.CircleList
  })
}

export const saveMoment = params => {
  return baseService({
    method: 'post',
    url: RequestUrl.MomentSave,
    params
  })
}

export const commentSave = params => {
  return baseService({
    method: 'post',
    url: RequestUrl.CommentSave,
    params
  })
}

export const getCommentList = params => {
  return baseService({
    method: 'post',
    url: RequestUrl.CommentList,
    params
  })
}
export const getMoments = params => {
  return baseService({
    method: 'post',
    url: RequestUrl.GetMoments,
    params
  })
}
