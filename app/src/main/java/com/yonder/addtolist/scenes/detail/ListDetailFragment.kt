package com.yonder.addtolist.scenes.detail

import android.view.LayoutInflater
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.yonder.addtolist.common.ui.base.BaseFragment
import com.yonder.addtolist.common.ui.extensions.addVerticalDivider
import com.yonder.addtolist.common.ui.extensions.openWithKeyboard
import com.yonder.addtolist.common.ui.extensions.removeAnimator
import com.yonder.addtolist.databinding.FragmentListDetailBinding
import com.yonder.addtolist.local.entity.ProductEntitySummary
import com.yonder.addtolist.local.entity.UserListProductEntity
import com.yonder.addtolist.local.entity.UserListWithProducts
import com.yonder.addtolist.scenes.detail.adapter.productlist.IProductOperation
import com.yonder.addtolist.scenes.detail.adapter.productlist.ProductListsAdapter
import com.yonder.statelayout.State
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect


/**
 * @author yusuf.onder
 * Created on 19.07.2021
 */

@AndroidEntryPoint
class ListDetailFragment : BaseFragment<FragmentListDetailBinding>() {

  private val args: ListDetailFragmentArgs by navArgs()

  private val viewModel: ListDetailViewModel by viewModels()

  private val listId get() = args.userList.id.toString()

  private val listUUID get() = args.userList.uuid

  val adapterProductList: ProductListsAdapter by lazy {
    ProductListsAdapter()
  }


  override fun initBinding(inflater: LayoutInflater) =
    FragmentListDetailBinding.inflate(inflater)

  override fun initObservers() {
    lifecycleScope.launchWhenResumed {
      viewModel.state.collect { viewState ->
        when (viewState) {
          ListDetailViewState.Loading -> {
            binding.stateLayout.setState(State.LOADING)
          }
          is ListDetailViewState.AddProduct -> {
            adapterProductList.addProduct(viewState.userListProductEntity)
          }
          is ListDetailViewState.ShowContent -> {
            binding.stateLayout.setState(State.CONTENT)
            binding.etSearch.openWithKeyboard(requireContext())
          }
          is ListDetailViewState.QueryResult -> {
            setQueryResult(viewState.list)
          }
          is ListDetailViewState.UserListContent -> {
            onUserListContent(viewState.userListWithProducts)
          }
          is ListDetailViewState.PopularProducts -> {
            setPopularProducts(viewState.list)
          }
          is ListDetailViewState.Error -> {
            binding.stateLayout.setState(State.ERROR)
          }
          is ListDetailViewState.Initial -> {
            binding.stateLayout.setState(State.CONTENT)
          }
        }
      }
    }
  }

  override fun initViews() {
    initEditText()
    initRecyclerView()
  }

  private fun initEditText() = with(binding.etSearch) {
    addTextChangedListener { editable ->
      val query = editable.toString()
      binding.tvSearchQuery.text = query
      viewModel.searchBy(query)
    }
  }

  private fun initRecyclerView() = with(binding.rvItems) {
    addVerticalDivider()
    removeAnimator()
    adapter = adapterProductList.apply {
      iProductOperation = object : IProductOperation {

        override fun decreaseProductQuantity(productEntity: UserListProductEntity) {
          viewModel.decreaseQuantity(listId, productEntity)
        }

        override fun increaseProductQuantity(productEntity: UserListProductEntity) {
          viewModel.increaseQuantity(listId, productEntity)
        }

        override fun removeProductEntity(productEntity: UserListProductEntity) {
          viewModel.removeProduct(productEntity)
        }

        override fun addProduct(product: ProductEntitySummary) {
          viewModel.addProduct(
            listId = listId,
            userListUUID = listUUID,
            product = product
          )
        }

      }
    }

  }

  private fun setQueryResult(list: List<ProductEntitySummary>) {
    setProductList(list, isHeaderVisible = false)
    binding.tvSearchQuery.isVisible = true
  }

  private fun onUserListContent(userListWithProducts: UserListWithProducts) {
    adapterProductList.userListProducts = ArrayList(userListWithProducts.products)
    adapterProductList.notifyDataSetChanged()
  }

  private fun setPopularProducts(list: List<ProductEntitySummary>) {
    setProductList(list, isHeaderVisible = true)
    binding.tvSearchQuery.isVisible = false
    viewModel.fetchProductsByUUID(listUUID)
  }

  private fun setProductList(list: List<ProductEntitySummary>, isHeaderVisible: Boolean) {
    binding.tvHeader.isInvisible = isHeaderVisible.not()
    adapterProductList.submitList(list)
  }


}