import { Button, Input, Upload, Image } from 'antd'
import { useState, useEffect, FC } from 'react'
import { useSelector } from 'react-redux'
import { commentSave, getCommentList } from '../../service'
import { CommentOutlined, FileImageOutlined, PlusOutlined, SmileOutlined } from '@ant-design/icons'
import './index.less'

const CommentInput = ({ momentId, getList, replyType, targetId = '', changeActive = null }) => {
  const [comment, setComment] = useState<string>('')
  const { userInfo } = useSelector(store => store.userInfo)
  const userInfoStorage = localStorage.getItem('userInfo')
  const { tokenValue = '' } = userInfoStorage ? JSON.parse(userInfoStorage) : {}
  const [imgList, setImgList] = useState([])

  const changeComment = e => {
    setComment(e.target.value)
  }

  const saveComment = () => {
    const params = {
      momentId,
      replyType,
      content: comment,
      targetId
    }
    if (imgList.length) {
      params.picUrlList = imgList.map(item => item.response.data)
    }
    commentSave(params).then(() => {
      setComment('')
      setImgList([])
      getList()
      changeActive?.(false)
    })
  }

  const uploadButton = (
    <button style={{ border: 0, background: 'none' }} type='button'>
      <PlusOutlined />
    </button>
  )
  const handleChange = ({ fileList }) => {
    setImgList(fileList)
  }
  return (
    <div className='comment-wrapper'>
      <img src={userInfo?.avatar} className='avatar' />
      <div className='text-area-outer-box'>
        <div className='text-area-box'>
          <Input.TextArea
            onChange={changeComment}
            placeholder='和平发言'
            style={{ border: 'none', paddingLeft: 0 }}
            maxLength={1000}
            value={comment}
          />
          <Upload
            name='uploadFile'
            action='/oss/upload'
            listType='picture-card'
            fileList={imgList}
            withCredentials
            headers={{
              satoken: 'jichi ' + tokenValue
            }}
            data={{
              bucket: 'user',
              objectName: 'icon'
            }}
            onChange={handleChange}
          >
            {imgList.length >= 8 || imgList.length === 0 ? null : uploadButton}
          </Upload>
        </div>
        <div className='comment-bottom'>
          <div className='icon-box flex'>
            <div style={{ marginRight: 20 }}>
              <SmileOutlined />
            </div>
            <div>
              <Upload
                name='uploadFile'
                className='avatar-uploader'
                accept='image/*'
                showUploadList={false}
                withCredentials
                action='/oss/upload'
                headers={{
                  satoken: 'jichi ' + tokenValue
                }}
                data={{
                  bucket: 'user',
                  objectName: 'icon'
                }}
                onChange={handleChange}
              >
                <div>
                  <FileImageOutlined />
                  {/* <span style={{ marginLeft: '8px' }}>图片</span> */}
                </div>
              </Upload>
              {/* <FileImageOutlined /> */}
            </div>
          </div>
          <div className='submit-btn-box flex'>
            <div className='text-num-box' style={{ marginRight: 20 }}>
              {comment.length}/1000
            </div>
            <Button onClick={saveComment} type='primary'>
              发送
            </Button>
          </div>
        </div>
      </div>
    </div>
  )
}

function formatDistanceToNow(date) {
  if (!date) return
  const delta = Math.abs(Date.now() - date)

  if (delta < 30 * 1000) {
    return '刚刚'
  } else if (delta < 5 * 60 * 1000) {
    return Math.round(delta / 1000) + '秒前'
  } else if (delta < 60 * 60 * 1000) {
    return Math.round(delta / 60000) + '分钟前'
  } else if (delta < 24 * 60 * 60 * 1000) {
    return Math.round(delta / 3600000) + '小时前'
  } else if (delta < 7 * 24 * 60 * 60 * 1000) {
    return Math.round(delta / 86400000) + '天前'
  } else {
    return new Date(date).toLocaleDateString()
    return '很久之前'
  }
}

const CommentItem = ({
  momentId,
  id,
  avatar,
  userName,
  content,
  createdTime,
  replyType,
  getList,
  children,
  picUrlList
}) => {
  const [active, setActive] = useState(false)
  const toggleActive = () => {
    setActive(!active)
  }
  return (
    <div className={`comment-item-wrapper`}>
      <div className='flex align-top'>
        <img src={avatar} className='avatar' alt='头像' />
        <div className='comment-detail-wrapper'>
          <div className='title'>{userName}</div>
          <div className='comment-content'>{content}</div>
          {picUrlList?.length && (
            <Image.PreviewGroup items={picUrlList}>
              <div className='comment-img-list'>
                {picUrlList.map((t: string) => (
                  <Image key={t} width={90} src={t} />
                ))}
              </div>
            </Image.PreviewGroup>
          )}
          <div className='comment-bottom-wrapper flex'>
            <div>{formatDistanceToNow(createdTime) || '12小时前'}</div>
            <div onClick={toggleActive} className={`bottom-btn ${active ? 'active' : ''}`}>
              <CommentOutlined />
              <span style={{ marginLeft: 5 }}>
                {active ? '取消回复' : replyType === 1 ? '评论' : '回复'}
              </span>
            </div>
          </div>
          {active && (
            <CommentInput
              momentId={momentId}
              getList={getList}
              targetId={id}
              replyType={2}
              changeActive={setActive}
            />
          )}
          {children?.length
            ? children?.map(item => {
                return <CommentItem key={item.id} {...item} getList={getList} />
              })
            : ''}
        </div>
      </div>
    </div>
  )
}

function flattenNestedObjects(items) {
  const result = []

  function traverse(items) {
    items.forEach(item => {
      // 创建一个新对象来存储当前项的属性（除了 children）
      const flatItem = {}
      for (const key in item) {
        if (key !== 'children') {
          flatItem[key] = item[key]
        }
      }
      // 将扁平化的对象添加到结果数组中
      result.push(flatItem)

      // 如果还有 children，则递归调用 traverse
      if (item.children) {
        traverse(item.children)
      }
    })
  }

  // 从顶层对象开始遍历
  traverse(items)

  return result
}

const CommentList: FC<any> = props => {
  const { momentId, replyCount } = props
  const [replyList, setReplyList] = useState<any[]>([])
  const getList = async () => {
    const res = await getCommentList({ id: momentId })
    if (res.success && res.data) {
      const data = res.data.map(item => {
        return {
          ...item,
          children: flattenNestedObjects(item.children || [])
        }
      })
      console.log(data, 'data')
      setReplyList(data)
    } else {
      setReplyList([])
    }
  }
  useEffect(() => {
    getList()
  }, [])

  return (
    <div className='comment-list-box'>
      <div className='top-arrow'></div>
      <div className='comment-number'>评论 {replyCount}</div>
      <CommentInput momentId={momentId} getList={getList} targetId={momentId} replyType={1} />
      <div className='comment-list-wrapper'>
        {replyList.map((item: Record<string, any>) => {
          return <CommentItem key={item.id} momentId={momentId} getList={getList} {...item} />
        })}
      </div>
    </div>
  )
}
export default CommentList
